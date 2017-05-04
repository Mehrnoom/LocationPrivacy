import org.apache.commons.math3.distribution.LaplaceDistribution;


public class LaplacianMechanism {
	
	public static double laplaceMechanismCount(double realCountResult, double sensitivity, double epsilon) {
		
		
	    LaplaceDistribution ld = new LaplaceDistribution(0, sensitivity/epsilon);
	    double noise = ld.sample();
	    return realCountResult + noise;

	}

}
