import java.util.ArrayList;


public class Checkin {
	public long userId;
	public Location exactPoint;
	public long poiId;
	
	public void assignPOI(ArrayList<Location> POIs) {
		LocationKDTree kdt = new LocationKDTree(POIs);
		double latitude = this.exactPoint.latitude;
		double longitude = this.exactPoint.longitude;
		this.poiId = kdt.findNearest(latitude, longitude).id;
	}
}
