import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.naming.NoInitialContextException;


public class Test {
	static ArrayList<Location> POIs = new ArrayList<Location>();
	static ArrayList<Checkin> checkins = new ArrayList<Checkin>();
	static ArrayList<Checkin> fakeCheckins = new ArrayList<Checkin>();
	
	static ArrayList<ArrayList<Double>> all = new ArrayList<ArrayList<Double>>();
	static DataAggregator da;
			
	public static void main(String[] args) throws IOException {
		
		Scanner sc = new Scanner(new File("POIs.csv"));
		int i = 1;
		while(sc.hasNext()) {
			String line = sc.nextLine();
			String[] arr = line.split(",");
			Location l = new Location();
			l.longitude  = Double.parseDouble(arr[0]);
			l.latitude = Double.parseDouble(arr[1]);
			l.id = i;
			
			POIs.add(l);
			i++;
		}
		
		sc = new Scanner(new File("USC.csv"));
		while(sc.hasNext()) {
			String line = sc.nextLine();
			String[] arr = line.split(",");
			Location l = new Location();
			l.longitude  = Double.parseDouble(arr[1]);
			l.latitude = Double.parseDouble(arr[2]);
			
			Checkin c = new Checkin(); 
			c.exactPoint = l;
			c.userId = Long.parseLong(arr[0]);
			c.assignPOI(POIs);
			
			checkins.add(c);
		}
		
		da = new DataAggregator(checkins);
		da.aggregateByUserId();
		Map<Long, Double> tp = da.popularity();
		
		/* Testing Trusted Curator Model */
		int number = Integer.parseInt(args[0]);
		double epsilon = Double.parseDouble(args[1]);

		
		String utilityAvrFileName = "utility_avr_" + epsilon + ".csv";
		String utilityMedFileName = "utility_med_" + epsilon + ".csv";
		String ratioFileName = "truncation_ratio_" + epsilon + ".csv";
		
		BufferedWriter utlAvrBw = new BufferedWriter(new FileWriter(utilityAvrFileName));
		BufferedWriter utlMedBw = new BufferedWriter(new FileWriter(utilityMedFileName));
		BufferedWriter ratioBw = new BufferedWriter(new FileWriter(ratioFileName));
		

		for (int k = 1; k < 30; k++) {
			double ratio = runTCM(number, k, epsilon, tp, utlMedBw, utlAvrBw);
			ratioBw.write(ratio + "\n");
		}
		utlAvrBw.close();
		utlMedBw.close(); 
		ratioBw.close();
		
		/* Testing Plannar Laplace */
		double l =  Double.parseDouble(args[2]);
		
		String utilityFileName = "utility_plannar_" + l + ".csv";
		BufferedWriter utlBw = new BufferedWriter(new FileWriter(utilityFileName));
		
		for (int r = 1; r < 10; r++) {
			runLDP(l, r/10.0, tp, utlBw);
		}
		utlBw.close();
	} 
	
	private static double runTCM(int number, int k, double epsilon, Map<Long, Double> tp, BufferedWriter utlMedBw, BufferedWriter utlAvrBw) throws IOException {		
		TrustedCuratorModel tcm = new TrustedCuratorModel(da);
		
		for (int i = 0; i < 3; i++) {
			all.add(new ArrayList<Double>());
		}
		
		double npSize = 0;
		for (int i = 0; i < number ; i++) {
			Map<Long, Double> noisyPopularity = tcm.addNoise(epsilon, k);
			
			all.get(0).add(UtilityMeasure.MAE(tp, noisyPopularity));
			all.get(1).add(UtilityMeasure.MSE(tp, noisyPopularity));
			all.get(2).add(UtilityMeasure.topKaccuracy(tp, noisyPopularity, 5));
			
			npSize= noisyPopularity.size();
		}
		
		utlMedBw.write(median(all.get(0)) + ", "
						+ median(all.get(1)) + ", "
						+ median(all.get(2))
						+ "\n");
		
		utlAvrBw.write(average(all.get(0)) + ", "
						+ average(all.get(1)) + ", "
						+ average(all.get(2))
						+ "\n");
		
		return (tp.size() - npSize)/tp.size();
	}
	
	private static void runLDP(double l, double r, Map<Long, Double> tp, BufferedWriter utlBw) throws IOException {
		double ratio = makeFakeCheckins(l/r);
		DataAggregator da = new DataAggregator(fakeCheckins);
		da.aggregateByUserId();
		Map<Long, Double> pl = da.popularity();
		
		utlBw.write(l + ", " + r + ", "
				+ UtilityMeasure.MAE(tp, pl)  + ", "
				+ UtilityMeasure.MSE(tp, pl)  + ", "
				+ UtilityMeasure.topKaccuracy(tp, pl, 5) + ", "
				+ ratio
				+ "\n");
	}
	
	
	
	private static double median(ArrayList<Double> array) {
		Collections.sort(array);
		int n = array.size();
		if (n == 0) {
			return -1;
		}
		if (n % 2 == 0) {
			return  array.get(n/2 -1) +  array.get(n/2);
		}
		else 
			return array.get((n-1)/2);
	}
	
	private static double average(ArrayList<Double> array) {
		if (array.size() == 0)
			return -1;
		else {
			double sum = 0;
		    for (int i = 0; i < array.size(); i++) {
				sum += array.get(i);
			}
		    
		    return sum / array.size(); 
		}
	}
	
	private static double makeFakeCheckins(double epsilon) {
		PlannarLaplace pl = new PlannarLaplace();
		double count = 0;
		for (int i = 0; i < checkins.size(); i++) {
			Location fake = pl.addNoise(epsilon, checkins.get(i).exactPoint);
			Checkin c = new Checkin();
			c.exactPoint = fake;
			c.userId = checkins.get(i).userId;
			c.assignPOI(POIs);
			if(c.poiId == checkins.get(i).poiId)
				count++;
			
			fakeCheckins.add(c);
		}
		
		return count/checkins.size();
	}
	
	private static void write(BufferedWriter bw, Map<Long, Double> map) throws IOException {
		String line = "", line0 = "";
		for(Map.Entry<Long, Double> p: map.entrySet()) {
			line0 += p.getKey() + ", ";
			line += p.getValue() + ", ";
		}
		
		line += "\n";
		line0 += "\n";
		bw.write(line0);
		bw.write(line);
	}
}
