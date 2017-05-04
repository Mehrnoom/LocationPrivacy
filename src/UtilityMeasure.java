import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class UtilityMeasure {
	
	public static double MSE(Map<Long, Double> truePopularity, Map<Long, Double> noisyPopularity) {
		double sum = 0;
		double count = 0;
		for(Map.Entry<Long, Double> p: truePopularity.entrySet()) {
			long key = p.getKey();
			if (noisyPopularity.containsKey(key) && noisyPopularity.get(key) != -1) {
				sum += Math.pow(p.getValue() - noisyPopularity.get(key), 2);
			}
			else {
				sum += Math.pow(p.getKey(), 2);
			}
			count ++;
		}
		
		return sum / count;
	}
	
	public static double MAE(Map<Long, Double> truePopularity, Map<Long, Double> noisyPopularity) {
		double sum = 0;
		double count = 0;
		for(Map.Entry<Long, Double> p: truePopularity.entrySet()) {
			long key = p.getKey();
			if (noisyPopularity.containsKey(key) && noisyPopularity.get(key) != -1) {
				sum += Math.abs(p.getValue() - noisyPopularity.get(key));
			}
			else {
				sum += p.getKey();
			}
			count ++;
		}
		
		return sum / count;
	}
	
	
	public static double topKaccuracy(Map<Long, Double> truePopularity, Map<Long, Double> noisyPopularity, int k) {
		double sum = 0;
		
		List<Entry<Long, Double>> sortedTp = sortMap(truePopularity);
		List<Entry<Long, Double>> sortedNp = sortMap(noisyPopularity);

		int i = 0;
		for(Map.Entry<Long, Double> tp: sortedTp) {
			if (i >= k)
				break;
			
			Long key = tp.getKey();
			int j = 0;
			for(Map.Entry<Long, Double> np: sortedNp) {
				if (j >= k)
					break;
				if (np.getKey() == key) {
					sum++;	
				}
				j++;
			}
			i++;
		}
		return sum / k;
	}
	
	private static List<Entry<Long, Double>> sortMap(Map<Long, Double> map) {
		Set<Entry<Long, Double>> set = map.entrySet();
        List<Entry<Long, Double>> list = new ArrayList<Entry<Long, Double>>(set);
        Collections.sort( list, new Comparator<Map.Entry<Long, Double>>()
        {
            public int compare( Map.Entry<Long, Double> o1, Map.Entry<Long, Double> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );
        
        return list;
	}
	

	
}
