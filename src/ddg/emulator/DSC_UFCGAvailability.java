package ddg.emulator;

import ddg.model.Availability;
import eduni.distributions.LogNormal;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public class DSC_UFCGAvailability implements Availability {
	
	private final LogNormal availabilityDistribution;
	private final LogNormal unavailabilityDistribution;
	
	public DSC_UFCGAvailability() {
		availabilityDistribution = new LogNormal(7.957307, 2.116613);
		unavailabilityDistribution = new LogNormal(7.242198, 1.034311);
	}

	@Override
	public long nextAvailabilityDuration() {
		return (long)(availabilityDistribution.sample() * 1000);
	}

	@Override
	public long nextUnavailabilityDuration() {
		return (long)(unavailabilityDistribution.sample() * 1000);
	}

}
