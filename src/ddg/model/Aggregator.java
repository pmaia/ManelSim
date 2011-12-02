package ddg.model;

import java.util.HashMap;
import java.util.Map;

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
		
		double totalActiveDuration = 0;
		double totalIdleDuration = 0;
		double totalSleepingDuration = 0;
		double totalShutdownDuration = 0;
		int shutdownCount = 0;
		int sleepCount = 0;
		
		for(String machine : availabilityTotalsPerMachine.keySet()) {
			MachineAvailability ma = availabilityTotalsPerMachine.get(machine);
			
			String format = "\nMachine=%s\tActive=%f ms\tIdle=%f ms\tSleeping=%f ms\tTurned off=%f ms\t" +
					"Shutdowns=%d\tSleepings=%d";
			
			summary.append(String.format(format, machine, ma.getTotalActiveDuration(), ma.getTotalIdleDuration(), 
					ma.getTotalSleepingDuration(), ma.getTotalShutdownDuration(), ma.getShutdownCount(), 
					ma.getSleepCount()));
			
			totalActiveDuration +=  ma.getTotalActiveDuration();
			totalIdleDuration += ma.getTotalIdleDuration();
			totalSleepingDuration += ma.getTotalSleepingDuration();
			totalShutdownDuration += ma.getTotalShutdownDuration();
			shutdownCount += ma.getShutdownCount();
			sleepCount += ma.getSleepCount();
		}
		
		summary.append(String.format("\n\nTotal active duration:\t%d ms",totalActiveDuration));
		summary.append(String.format("\n\nTotal idle duration:\t%d ms", totalIdleDuration));
		summary.append(String.format("\n\nTotal sleeping duration:\t%d ms", totalSleepingDuration));
		summary.append(String.format("\n\nTotal turned off duration:\t%d ms", totalShutdownDuration));
		summary.append(String.format("\n\nTotal shutdowns:\t%d ms", shutdownCount));
		summary.append(String.format("\n\nTotal sleeps:\t%d ms", sleepCount));
		
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
		
		return summary.toString();
	}
	
}