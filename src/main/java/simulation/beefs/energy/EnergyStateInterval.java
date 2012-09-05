package simulation.beefs.energy;

import core.TimeInterval;

public class EnergyStateInterval {
	private final EnergyState state;
	private final TimeInterval interval;
	
	public EnergyStateInterval(EnergyState state, TimeInterval interval) {
		this.state = state;
		this.interval = interval;
	}
	
	public EnergyState getEnergyState() {
		return state;
	}
	
	public TimeInterval getInterval() {
		return interval;
	}
}