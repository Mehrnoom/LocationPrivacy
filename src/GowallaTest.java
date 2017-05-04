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


public class GowallaTest {
	static ArrayList<Location> POIs = new ArrayList<Location>();
	static ArrayList<Checkin> checkins = new ArrayList<Checkin>();
	static ArrayList<Checkin> fakeCheckins = new ArrayList<Checkin>();
	static Map<Long, ArrayList<Double>> all = new HashMap<Long, ArrayList<Double>>();
	
	public static void main(String[] args) throws IOException {
		
		Scanner sc = new Scanner(new File("new.txt"));
		int i = 1;
		while(sc.hasNext()) {
			String line = sc.nextLine();
			String[] arr = line.split("\t");
			Location l = new Location();
			l.longitude  = Double.parseDouble(arr[3]);
			l.latitude = Double.parseDouble(arr[2]);
			l.id = Long.parseLong(arr[4]);
			
			POIs.add(l);
			i++;
		}
		
		sc = new Scanner(new File("new.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("Gowalla.txt"));
		int r=0;
		while(sc.hasNext()) {
			String line = sc.nextLine();
			String[] arr = line.split("\t");
			Location l = new Location();
			l.longitude  = Double.parseDouble(arr[3]);
			l.latitude = Double.parseDouble(arr[2]);
			
			Checkin c = new Checkin();
			c.exactPoint = l;
//			c.userId = Long.parseLong(arr[0]);
//			c.assignPOI(POIs);
			
			String myline = l.latitude + ", " + l.longitude + "\n";
			bw.write(myline);
			
			checkins.add(c);
			System.out.println(r++);
		}
		
		bw.close();
		
		
		/* Testing Trusted Curator Model */
		int number = Integer.parseInt(args[0]);
		double epsilon = Double.parseDouble(args[1]);
		DataAggregator da = new DataAggregator(checkins);
		da.aggregateByUserId();
		Map<Long, Double> tp = da.popularity();
		TrustedCuratorModel tcm = new TrustedCuratorModel(da);
		
		String medianFileName = "median_" + epsilon + ".csv";
		String averageFileName = "average_" + epsilon + ".csv";
		String utilityAvrFileName = "utility_avr_" + epsilon + ".csv";
		String utilityMedFileName = "utility_med_" + epsilon + ".csv";
		
		BufferedWriter medBw = new BufferedWriter(new FileWriter(medianFileName));
		BufferedWriter avrBw = new BufferedWriter(new FileWriter(averageFileName));
		BufferedWriter utlAvrBw = new BufferedWriter(new FileWriter(utilityAvrFileName));
		BufferedWriter utlMedBw = new BufferedWriter(new FileWriter(utilityMedFileName));
		
		write(medBw, tp);
		write(avrBw, tp);
		for (int k = 1; k < 30; k++) {
			System.out.println(k);
			run(number, k, epsilon, da);
			
			Map<Long, Double> nm = noisyMedian();
			Map<Long, Double> na = noisyAverage();
			
			write(medBw, nm);
			write(avrBw, na);
			
//			Map<Long, Double> nm = tcm.addNoise(epsilon, k);
//			write(medBw, nm);
			
			utlMedBw.write(UtilityMeasure.MAE(tp, nm) + ", "
							+ UtilityMeasure.MSE(tp, nm) + ", "
							+ UtilityMeasure.topKaccuracy(tp, nm, 5)
							+ "\n");

			utlAvrBw.write(UtilityMeasure.MAE(tp, na) + ", "
							+ UtilityMeasure.MSE(tp, na) + ", "
							+ UtilityMeasure.topKaccuracy(tp, na, 5)
							+ "\n");
		}
		
		medBw.close();
		avrBw.close();
		utlAvrBw.close();
		utlMedBw.close(); 
		
		
		/* Testing Plannar Laplace */
		makeFakeCheckins(0.001);
		DataAggregator plDa = new DataAggregator(fakeCheckins);
		plDa.aggregateByUserId();
		Map<Long, Double> pl = plDa.popularity();
		
		String utilityFileName = "utility_plannar_" + epsilon + ".csv";
		String fkCheckinsFileName = "fake_checkins_" + epsilon + ".csv";
		String plPopularityFileName = "plannar_" + epsilon + ".csv";
		
		BufferedWriter utlBw = new BufferedWriter(new FileWriter(utilityFileName));
		BufferedWriter fkBw = new BufferedWriter(new FileWriter(fkCheckinsFileName));
		BufferedWriter plBw = new BufferedWriter(new FileWriter(plPopularityFileName));
		
		write(plBw, pl);
		
		utlBw.write(UtilityMeasure.MAE(tp, pl) + ", "
				+ UtilityMeasure.MSE(tp, pl) + ", "
				+ UtilityMeasure.topKaccuracy(tp, pl, 5)
				+ "\n");
		
		for (int j = 0; j < fakeCheckins.size(); j++) {
			String line = fakeCheckins.get(j).exactPoint.latitude + ", " +
					fakeCheckins.get(j).exactPoint.longitude + "\n";
			fkBw.write(line);
		}
		
		utlBw.close();
		plBw.close();
		fkBw.close();
	} 
	
	private static void run(int number, int k, double epsilon, DataAggregator da) {		
		TrustedCuratorModel tcm = new TrustedCuratorModel(da);
				
		for (int i = 0; i < POIs.size() ; i++) {
			all.put(POIs.get(i).id, new ArrayList<Double>());
		}
		
		for (int i = 0; i < number ; i++) {
			Map<Long, Double> noisyPopularity = tcm.addNoise(epsilon, k);
			for(Map.Entry<Long, Double> p: noisyPopularity.entrySet()){
				long key = p.getKey();
				if (p.getValue() <= 0)
					all.get(key).add(0.0);
				else {
					all.get(key).add(p.getValue());
				}
			}
		}
		
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
	
	private static Map<Long, Double> noisyMedian() {
		Map<Long, Double> nm = new HashMap<Long, Double>();
		
		for(Map.Entry<Long, ArrayList<Double>> p: all.entrySet()) {
			nm.put(p.getKey(), median(p.getValue()));
		}
		return nm;
	}
	
	private static Map<Long, Double> noisyAverage() {
		Map<Long, Double> na = new HashMap<Long, Double>();
		for(Map.Entry<Long, ArrayList<Double>> p: all.entrySet()) {
			na.put(p.getKey(), average(p.getValue()));
		}
		return na;
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
	
	private static void makeFakeCheckins(double epsilon) {
		PlannarLaplace pl = new PlannarLaplace();
		for (int i = 0; i < checkins.size(); i++) {
			Location fake = pl.addNoise(epsilon, checkins.get(i).exactPoint);
			Checkin c = new Checkin();
			c.exactPoint = fake;
			c.userId = checkins.get(i).userId;
			c.assignPOI(POIs);
			
			fakeCheckins.add(c);
		}
	}
}
