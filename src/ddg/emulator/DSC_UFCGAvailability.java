package ddg.emulator;

import ddg.kernel.JETime;
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
	
	private boolean available;

	private JETime now;
	private JETime end;

	public DSC_UFCGAvailability() {
		availabilityDistribution = new LogNormal(7.957307, 2.116613);
		unavailabilityDistribution = new LogNormal(7.242198, 1.034311);
		
		if(System.currentTimeMillis() % 2 == 0)
			available = true;
	}

	private long nextAvailabilityDuration() {
		return (long)(availabilityDistribution.sample() * 1000);
	}

	private long nextUnavailabilityDuration() {
		return (long)(unavailabilityDistribution.sample() * 1000);
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public void updateSimulationTime(JETime now) {
		if(this.now != null && now.isEarlierThan(this.now))
			throw new IllegalArgumentException();
		
		if(end == null) {
			end = now;
		}
		
		while(now.compareTo(end) >= 0) {
			JETime increment;
			if(available) {
				increment = new JETime(nextUnavailabilityDuration());
			} else {
				increment = new JETime(nextAvailabilityDuration());
			}

			end = end.plus(increment);
			available = !available;
		}
		
		this.now = now;
	}

	@Override
	public JETime getSimulationTime() {
		return now;
	}

}
