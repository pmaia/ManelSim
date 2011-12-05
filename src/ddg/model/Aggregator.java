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
	
	public void aggregateActiveDuration(String machine, Time activeDuration) {
		getMachineAvailability(machine).addActiveDuration(activeDuration);
	}
	
	public void aggregateIdleDuration(String machine, Time idleDuration) {
		getMachineAvailability(machine).addIdleDuration(idleDuration);
	}
	
	public void aggregateSleepingDuration(String machine, Time sleepingDuration) {
		getMachineAvailability(machine).addSleepingDuration(sleepingDuration);
	}
	
	public void aggregateShutdownDuration(String machine, Time shutdownDuration) {
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
		
		long totalActiveDuration = 0;
		long totalIdleDuration = 0;
		long totalSleepingDuration = 0;
		long totalShutdownDuration = 0;
		int shutdownCount = 0;
		int sleepCount = 0;
		
		for(String machine : availabilityTotalsPerMachine.keySet()) {
			MachineAvailability ma = availabilityTotalsPerMachine.get(machine);
			
			long machineActiveDuration =  ma.getTotalActiveDuration().asMicroseconds();
			long machineIdleDuration = ma.getTotalIdleDuration().asMicroseconds();
			long machineSleepingDuration = ma.getTotalSleepingDuration().asMicroseconds();
			long machineShutdownDuration = ma.getTotalShutdownDuration().asMicroseconds();
			int machineShutdownCount = ma.getShutdownCount();  
			int machineSleepCount = ma.getSleepCount();
			
			String format = "\nMachine=%s\tActive=%d us\tIdle=%d us\tSleeping=%d us\tTurned off=%d us\t" +
					"Shutdowns=%d\tSleepings=%d";
			
			summary.append(String.format(format, machine, machineActiveDuration, machineIdleDuration, 
					machineSleepingDuration, machineShutdownDuration, machineShutdownCount, machineSleepCount));
			
			totalActiveDuration +=  machineActiveDuration;
			totalIdleDuration += machineIdleDuration;
			totalSleepingDuration += machineSleepingDuration;
			totalShutdownDuration += machineShutdownDuration;
			shutdownCount += machineShutdownCount;
			sleepCount += machineSleepCount;
		}
		
		summary.append(String.format("\n\nTotal active duration:\t%d us",totalActiveDuration));
		summary.append(String.format("\nTotal idle duration:\t%d us", totalIdleDuration));
		summary.append(String.format("\nTotal sleeping duration:\t%d us", totalSleepingDuration));
		summary.append(String.format("\nTotal turned off duration:\t%d us", totalShutdownDuration));
		summary.append(String.format("\nTotal shutdowns:\t%d", shutdownCount));
		summary.append(String.format("\nTotal sleeps:\t%d", sleepCount));
		
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