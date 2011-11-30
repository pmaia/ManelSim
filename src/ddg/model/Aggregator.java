package ddg.model;

import java.util.HashMap;
import java.util.Map;

import ddg.kernel.Time;

public class Aggregator {

	private final Map<String, MachineAvailability> availabilityTotalsPerMachine = 
		new HashMap<String, MachineAvailability>();

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
	
	public void aggregateActiveDuration(String machine, double activeDuration) {
		getMachineAvailability(machine).addActiveDuration(activeDuration);
	}
	
	public void aggregateIdleDuration(String machine, double idleDuration) {
		getMachineAvailability(machine).addIdleDuration(idleDuration);
	}
	
	public void aggregateSleepingDuration(String machine, double sleepingDuration) {
		getMachineAvailability(machine).addSleepingDuration(sleepingDuration);
	}
	
	public void aggregateShutdownDuration(String machine, double shutdownDuration) {
		getMachineAvailability(machine).addShutdownDuration(shutdownDuration);
	}
	
	public void aggregateToSleepDuration(String machine, double toSleepDuration) {
		getMachineAvailability(machine).addToSleepDuration(toSleepDuration);
	}
	
	public void aggregateFromSleepDuration(String machine, double fromSleepDuration) {
		getMachineAvailability(machine).addFromSleepDuration(fromSleepDuration);
	}
	
	public void aggregateToShutdownDuration(String machine, double toShutdownDuration) {
		getMachineAvailability(machine).addToShutdownDuration(toShutdownDuration);
	}
	
	public void aggregateFromShutdownDuration(String machine, double fromShutdownDuration) {
		getMachineAvailability(machine).addFromShutdownDuration(fromShutdownDuration);
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
//		StringBuilder summary = new StringBuilder();
//		
//		//summarize availability
//		summary.append("\n\n============================================ \nAvailability summary: \n");
//		
//		long totalActiveDurationMillis = 0;
//		long totalInactiveDurationMillis = 0;
//		long totalTransitions = 0;
//		
//		for(String machine : availabilityTotalsPerMachine.keySet()) {
//			MachineAvailability mAvailability = availabilityTotalsPerMachine.get(machine);
//			
//			summary.append(String.format("\nMachine=%s\tActive=%d ms\tInactive=%d ms\tTransitions=%d", 
//					machine, mAvailability.getTotalActiveDuration(), mAvailability.getTotalSleepingDuration(), 
//					mAvailability.getTransitionsCount()));
//			
//			totalActiveDurationMillis += mAvailability.getTotalActiveDuration();
//			totalInactiveDurationMillis += mAvailability.getTotalSleepingDuration();
//			totalTransitions += mAvailability.getTransitionsCount();
//		}
//		
//		summary.append(String.format("\n\nTotal active duration:\t%d ms", totalActiveDurationMillis));
//		summary.append(String.format("\nTotal inactive duration:\t%d ms", totalInactiveDurationMillis));
//		summary.append(String.format("\nTotal transitions:\t%d", totalTransitions));
//		
//		//summarize energy consumption
//		summary.append("\n\n============================================ \nEnergy consumption summary: \n");
//		double activeConsumptionWh = toHours(totalActiveDurationMillis) * ACTIVE_POWER_IN_WATTS;
//		double inactiveConsumptionWh = toHours(totalInactiveDurationMillis) * STAND_BY_POWER_IN_WATTS;
//		double transitionConsumptionWh = 
//			totalTransitions * toHours(TRANSITION_DURATION) * TRANSITION_POWER_IN_WATTS;
//		
//		double energyConsumptionkWh = (activeConsumptionWh + inactiveConsumptionWh + transitionConsumptionWh) / 1000; 
//		
//		summary.append(String.format("\nEnergy consumption without opportunistic distributed file system:\t%f kWh",
//				energyConsumptionkWh));
//		
//		return summary.toString();
		return null; //TODO implementar
	}
	
	private double toHours(double timeInMillis) {
		return ((timeInMillis / 1000) / 60) / 60;
	}
	
	private double toHours(Time time) {
		return toHours(time.asMilliseconds());
	}
	
}