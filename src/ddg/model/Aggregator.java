package ddg.model;

import java.util.HashMap;
import java.util.Map;

public class Aggregator {

	private final Map<String, MachineAvailability> availabilityTotalsPerMachine = new HashMap<String, MachineAvailability>();

	private static Aggregator instance = new Aggregator();

	public static Aggregator getInstance() {
		return instance;
	}

	private Aggregator() { /* empty */ }
	
	/**
	 * Clean all aggregated result. This method exists to make testing easy.
	 */
	public void reset() {
		availabilityTotalsPerMachine.clear();
	}
	
	public void aggregateActiveDuration(String machine, long activeDuration) {
		getMachineAvailability(machine).addActiveDuration(activeDuration);
	}
	
	public void aggregateSleepingDuration(String machine, long sleepingDuration) {
		getMachineAvailability(machine).addSleepingDuration(sleepingDuration);
	}
	
	public MachineAvailability getMachineAvailability(String machine) {
		MachineAvailability machineAvailability = availabilityTotalsPerMachine.get(machine);
		if(machineAvailability == null) {
			machineAvailability = new MachineAvailability();
			availabilityTotalsPerMachine.put(machine, machineAvailability);
		}
		
		return machineAvailability;
	}
	
	public String summarize() {
		StringBuilder summary = new StringBuilder();
		
		//summarize availability
		summary.append("\n\n============================================ \nAvailability summary: \n");
		
		long totalActiveDurationMillis = 0;
		long totalInactiveDurationMillis = 0;
		long totalTransitions = 0;
		
		for(String machine : availabilityTotalsPerMachine.keySet()) {
			MachineAvailability mAvailability = availabilityTotalsPerMachine.get(machine);
			
			summary.append(String.format("\nMachine=%s\tActive=%d ms\tInactive=%d ms\tTransitions=%d", 
					machine, mAvailability.getActiveDurationTotal(), mAvailability.getSleepingDurationTotal(), 
					mAvailability.getTransitionsCount()));
			
			totalActiveDurationMillis += mAvailability.getActiveDurationTotal();
			totalInactiveDurationMillis += mAvailability.getSleepingDurationTotal();
			totalTransitions += mAvailability.getTransitionsCount();
		}
		
		summary.append(String.format("\n\nTotal active duration:\t%d ms", totalActiveDurationMillis));
		summary.append(String.format("\nTotal inactive duration:\t%d ms", totalInactiveDurationMillis));
		summary.append(String.format("\nTotal transitions:\t%d", totalTransitions));
		
		//summarize energy consumption
//		summary.append("\n\n============================================ \nEnergy consumption summary: \n");
//		double activeConsumptionWh = toHours(totalActiveDurationMillis) * ACTIVE_POWER_IN_WATTS;
//		double inactiveConsumptionWh = toHours(totalInactiveDurationMillis) * STAND_BY_POWER_IN_WATTS;
//		double transitionConsumptionWh = 
//			totalTransitions * toHours(TRANSITION_DURATION_IN_MILLISECONDS) * TRANSITION_POWER_IN_WATTS;
//		
//		double energyConsumptionkWh = (activeConsumptionWh + inactiveConsumptionWh + transitionConsumptionWh) / 1000; 
//		
//		summary.append(String.format("\nEnergy consumption without opportunistic distributed file system:\t%f kWh",
//				energyConsumptionkWh));
//		
//		double perturbationConsumptionkWh = 
//			totalPerturbationCount * toHours(TRANSITION_DURATION_IN_MILLISECONDS) * TRANSITION_POWER_IN_WATTS;
//		perturbationConsumptionkWh += (toHours(totalPerturbationDuration) * ACTIVE_POWER_IN_WATTS);
//		
//		double energyConsumptionDOFSkWh = perturbationConsumptionkWh + energyConsumptionkWh;
//		
//		summary.append(String.format("\nEnergy consumption with opportunistic distributed file system:\t%f kWh",
//				energyConsumptionDOFSkWh));

		return summary.toString();
	}
	
	private double toHours(long timeInMillis) {
		return ((timeInMillis / 1000) / 60) / 60;
	}
	
}