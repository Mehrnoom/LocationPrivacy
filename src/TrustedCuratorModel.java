import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class TrustedCuratorModel {
	DataAggregator da;
	Map<Long, Long> popularities;
	
	public TrustedCuratorModel(DataAggregator da) {
		this.da = da;
	}
		
	
	
	public Map<Long, Double> addNoise(double epsilon, int k) {
//		System.out.println("addNoise");
		Map<Long, Double> popularity = da.popularity(k);
		Map<Long, Double> noisyPopularity = new HashMap<Long, Double>();
		
		for (Map.Entry<Long, Double> entry: popularity.entrySet()) {
			double noisyValue = LaplacianMechanism.laplaceMechanismCount(entry.getValue(), k, epsilon);
			if (noisyValue < 0)
				noisyValue = 0;
			noisyPopularity.put(entry.getKey(), noisyValue);
//			System.out.println(entry.getKey() + ": " + entry.getValue() + ", "  + noisyValue);
		}
		
		return noisyPopularity;
	}
	
	
	
}
