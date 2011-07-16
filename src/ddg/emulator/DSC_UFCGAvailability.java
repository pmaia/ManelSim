package ddg.emulator;

import ddg.model.Availability;
import ddg.model.State;
import eduni.distributions.LogNormal;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public class DSC_UFCGAvailability implements Availability {

	private final LogNormal inactiveDurationDistribution;
	private final LogNormal activeDurationDistribution;
	
	private State currentState;
	
	public DSC_UFCGAvailability() {
		inactiveDurationDistribution = new LogNormal(7.957307, 2.116613);
		activeDurationDistribution = new LogNormal(7.242198, 1.034311);
		
		if(System.currentTimeMillis() % 2 == 0) {
			currentState = new State(true, nextActiveDuration());
		} else {
			currentState = new State(false, nextInactiveDuration());
		}
	}

	private long nextInactiveDuration() {
		return (long)(inactiveDurationDistribution.sample() * 1000);
	}

	private long nextActiveDuration() {
		return (long)(activeDurationDistribution.sample() * 1000);
	}

	@Override
	public State currentState() {
		return currentState;
	}

	@Override
	public void advanceState() {
		if(currentState.isActive()) {
			currentState = new State(false, nextInactiveDuration());
		} else {
			currentState = new State(true, nextActiveDuration());
		}
	}
}
