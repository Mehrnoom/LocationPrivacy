import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class DataAggregator{
	Map<Long, UserRecord> userRecords = new HashMap();
	ArrayList<Checkin> checkins = new ArrayList<Checkin>();
	
	
	public DataAggregator(ArrayList<Checkin> checkins) {
		this.checkins = checkins;
	}
	
	public void aggregateByUserId() {
		for (int i = 0; i < checkins.size() ; i++) {
			long userId = checkins.get(i).userId;
			long locationId = checkins.get(i).poiId; 
			
			if (!userRecords.containsKey(userId)) 
				userRecords.put(userId, new UserRecord());
				
			Map<Long, Long> locationVisits = userRecords.get(userId).locationVists;
				
			if (locationVisits.containsKey(locationId)) {
				Long numberOfVisits = locationVisits.get(locationId);
				locationVisits.put(locationId, numberOfVisits + 1);
			}
			else {
				locationVisits.put(locationId, 1l);
			}
			
		}
	}
	
		
	
	
	public Map<Long, Double> popularity() {
		Map<Long, Double> locationPopularity = new HashMap<Long, Double>();
		for(Map.Entry<Long, UserRecord> user : userRecords.entrySet()) {
			for(Map.Entry<Long, Long> location : user.getValue().locationVists.entrySet()) {
				long key = location.getKey();
				if (locationPopularity.containsKey(key)){
					double tmp = locationPopularity.get(key);
					locationPopularity.put(key, tmp + 1);
				}
				else {
					locationPopularity.put(key, 1.0);
				}
			}
		}
		
		return locationPopularity;
	}
	
	
	public Map<Long, Double> popularity(int k) {
		Map<Long, Double> locationPopularity = new HashMap<Long, Double>();
		for(Map.Entry<Long, UserRecord> user : userRecords.entrySet()) {
			int totalVisit = 0;
			boolean flag = false;
			for(Map.Entry<Long, Long> location : user.getValue().locationVists.entrySet()) {
				if (flag)
					break;
				
 				long locationId = location.getKey();
 				double userVisitOfLocation = location.getValue();
 				totalVisit += userVisitOfLocation;
 				
 				if (totalVisit >= k) {
 					userVisitOfLocation -= totalVisit - k;
 					flag = true;
 				}
 				
 				
				if (locationPopularity.containsKey(locationId)){
					double tmp = locationPopularity.get(locationId);
					locationPopularity.put(locationId, tmp + userVisitOfLocation);
				}
				else {
					locationPopularity.put(locationId, userVisitOfLocation);
					
				}
			}
		}
		
		return locationPopularity;
	}
	
	
}
