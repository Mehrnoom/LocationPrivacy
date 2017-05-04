
public class PlannarLaplace {
	private static long EARTH_RADIUS = 6378137; //const, in meters
	

	// convert an angle in radians to degrees and viceversa
	private double radOfDeg(double ang) {
		return ang * Math.PI / 180;
	}
	
	private double degOfRad(double ang){
		return ang * 180 / Math.PI;
	}

	// Mercator projection 
	// https://wiki.openstreetmap.org/wiki/Mercator
	// https://en.wikipedia.org/wiki/Mercator_projection

	//getLatLon and getCartesianPosition are inverse functions
	//They are used to transfer { x: ..., y: ... } and { latitude: ..., longitude: ... } into one another
	public Location getLatLon(Cartesian pos) {
		double rLon = pos.x / EARTH_RADIUS;
		double rLat = 2 * (Math.atan(Math.exp(pos.y / EARTH_RADIUS))) - Math.PI/2;
		//convert to degrees
		
		return new Location(degOfRad(rLat), degOfRad(rLon));
	}

	public Cartesian getCartesian(Location ll){
		// latitude and longitude are converted in radiants
		return new Cartesian(
			EARTH_RADIUS * this.radOfDeg(ll.longitude),
			EARTH_RADIUS * Math.log( Math.tan(Math.PI / 4 + this.radOfDeg(ll.latitude) / 2)));
	}


	// LamberW function on branch -1 (http://en.wikipedia.org/wiki/Lambert_W_function)
	public double LambertW(double x){
		//min_diff decides when the while loop should stop
		double min_diff = 1e-10;
		if (x == -1/Math.E){
			return -1;
		}

		else if (x<0 && x>-1/Math.E) {
			double q = Math.log(-x);
			double p = 1;
			while (Math.abs(p-q) > min_diff) {
				p=(q*q+x/Math.exp(q))/(q+1);
				q=(p*p+x/Math.exp(p))/(p+1);
			}
			//This line decides the precision of the float number that would be returned
			return (Math.round(1000000*q)/1000000);
		}
		else if (x==0) {return 0;}
		//TODO why do you need this if branch? 
		else{
			return 0;
		}
	}

	// This is the inverse cumulative polar laplacian distribution function. 
	public double inverseCumulativeGamma(double epsilon, double z){
		double x = (z-1) / Math.E;
		return - (this.LambertW(x) + 1) / epsilon;
	}

	// returns alpha such that the noisy pos is within alpha from the real pos with
	// probability at least delta
	// (comes directly from the inverse cumulative of the gamma distribution)
	//
	public double alphaDeltaAccuracy(double epsilon, double delta) {
		return this.inverseCumulativeGamma(epsilon, delta);
	}

	// returns the average distance between the real and the noisy pos
	//
	public double expectedError(double epsilon) {
		return 2 / epsilon;
	}


	public Location addPolarNoise(double epsilon, Location pos) {
		//random number in [0, 2*PI)
		double theta = Math.random() * Math.PI * 2;
		//random variable in [0,1)
		double z = Math.random();
		double r = this.inverseCumulativeGamma(epsilon, z);
		
		return this.addVectorToPos(pos, r, theta);
	}

	public Location addPolarNoiseCartesian(double epsilon, Cartesian pos) {
//		if('latitude' in pos)
//			pos = this.getCartesian(pos);

		//random number in [0, 2*PI)
		double theta = Math.random() * Math.PI * 2;
		//random variable in [0,1)
		double z = Math.random();
		double r = this.inverseCumulativeGamma(epsilon, z);
		

		return getLatLon(new Cartesian(
			pos.x + r * Math.cos(theta),
			pos.y + r * Math.sin(theta)
		));
	}

	// http://www.movable-type.co.uk/scripts/latlong.html
	public Location addVectorToPos(Location pos, double distance, double angle) {
		double ang_distance = distance / EARTH_RADIUS;
		double lat1 = this.radOfDeg(pos.latitude);
		double lon1 = this.radOfDeg(pos.longitude);

		double	lat2 =	Math.asin(
						Math.sin(lat1) * Math.cos(ang_distance) + 
						Math.cos(lat1) * Math.sin(ang_distance) * Math.cos(angle)
				  	);
		double lon2 =	lon1 +
				   	Math.atan2(
						Math.sin(angle) * Math.sin(ang_distance) * Math.cos(lat1), 
						Math.cos(ang_distance) - Math.sin(lat1) * Math.sin(lat2)
					);
		
	
		lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;		// normalise to -180..+180
		return new Location (this.degOfRad(lat2), this.degOfRad(lon2));
	}


	//This function generates the position of a point with Laplacian noise
	//
	public Location addNoise(double epsilon, Location pos) {
		// TODO: use latlon.js
		return this.addPolarNoise(epsilon, pos);
	}

}
