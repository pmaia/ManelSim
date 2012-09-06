package simulation.beefs.energy;

import java.util.List;

/**
 * 
 * @author Patrick Maia
 *
 */
public interface EnergyConsumptionModel {
	/**
	 * 
	 * @param intervals
	 * @return the total energy consumption in kilowatts-hour 
	 */
	double getConsumption(List<EnergyStateInterval> intervals);
}
