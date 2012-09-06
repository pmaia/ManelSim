package simulation.beefs.energy;

import java.util.List;

import core.Time;

/**
 * 
 * @author Patrick Maia
 *
 */
public class Conservative implements EnergyConsumptionModel {
	
	/*
	 * Consumption values from a Dell OptiPlex 740, w/19-inch Dell LCD (purchased late 2006)
	 * Source: http://www.upenn.edu/computing/provider/docs/hardware/powerusage.html
	 */
	
	private static final int SLEEPING_CONSUMPTION = 3;
	private static final int PEAK_CONSUMPTION = 151;
	private static final int MODERATE_USAGE = 108;
	

	@Override
	public double getConsumption(List<EnergyStateInterval> intervals) {
		Time sleepingTime = Time.GENESIS;
		Time peakTime = Time.GENESIS;
		Time moderateUsageTime = Time.GENESIS;		
		
		for(EnergyStateInterval energyStateInterval : intervals) {
			switch(energyStateInterval.getEnergyState()) {
				case TRANSITIONING:
				case READ_ACTIVE: 
				case WRITE_ACTIVE:
				case READ_IDLE: 
				case WRITE_IDLE:
				case READ_WRITE_IDLE:
				case READ_WRITE_ACTIVE: peakTime = peakTime.plus(energyStateInterval.getInterval().delta()); break; 
				case ACTIVE: 
				case IDLE: moderateUsageTime = moderateUsageTime.plus(energyStateInterval.getInterval().delta()); break;
				case SLEEPING: sleepingTime = sleepingTime.plus(energyStateInterval.getInterval().delta()); break;
			}
		}

		long energyConsumption = sleepingTime.asHours() * SLEEPING_CONSUMPTION;
		energyConsumption += peakTime.asHours() * PEAK_CONSUMPTION;
		energyConsumption += moderateUsageTime.asHours() * MODERATE_USAGE;
				
		return energyConsumption / 1000.0; //convert to kWh
	}


}
