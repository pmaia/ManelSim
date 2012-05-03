package model;

import static model.Machine.ACTIVE_POWER_IN_WATTS;
import static model.Machine.IDLE_POWER_IN_WATTS;
import static model.Machine.SLEEP_POWER_IN_WATTS;
import static model.Machine.SLEEP_TRANSITION_DURATION;
import static model.Machine.TRANSITION_POWER_IN_WATTS;

import java.util.HashMap;
import java.util.Map;

import kernel.Time;


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
		
		Time totalActiveDuration = Time.GENESIS;
		Time totalIdleDuration = Time.GENESIS;
		Time totalSleepingDuration = Time.GENESIS;
		
		int sleepCount = 0;
		
		for(String machine : availabilityTotalsPerMachine.keySet()) {
			MachineAvailability ma = availabilityTotalsPerMachine.get(machine);
			
			Time machineActiveDuration =  ma.getTotalActiveDuration();
			Time machineIdleDuration = ma.getTotalIdleDuration();
			Time machineSleepingDuration = ma.getTotalSleepingDuration();

			int machineSleepCount = ma.getSleepCount();
			
			String format = "\nMachine=%s\tActive=%s us\tIdle=%s us\tSleeping=%s us\tSleepings=%d";
			
			summary.append(String.format(format, machine, machineActiveDuration, machineIdleDuration, 
					machineSleepingDuration, machineSleepCount));
			
			totalActiveDuration =  totalActiveDuration.plus(machineActiveDuration);
			totalIdleDuration = totalIdleDuration.plus(machineIdleDuration);
			totalSleepingDuration = totalSleepingDuration.plus(machineSleepingDuration);
			sleepCount += machineSleepCount;
		}
		
		summary.append(String.format("\n\nTotal active duration:\t%s us",totalActiveDuration));
		summary.append(String.format("\nTotal idle duration:\t%s us", totalIdleDuration));
		summary.append(String.format("\nTotal sleeping duration:\t%s us", totalSleepingDuration));
		summary.append(String.format("\nTotal sleeps:\t%d", sleepCount));
		
		//summarize energy consumption
		summary.append("\n\n============================================ \nEnergy consumption summary: \n");
		double activeConsumptionWh = totalActiveDuration.asHours() * ACTIVE_POWER_IN_WATTS;
		
		double idleConsumptionWh = totalIdleDuration.asHours() * IDLE_POWER_IN_WATTS;
		
		Time totalSleepTransition = SLEEP_TRANSITION_DURATION.times(sleepCount * 2); //each sleep corresponds to two transitions
		double sleepTransitionConsumptionWh = totalSleepTransition.asHours() * TRANSITION_POWER_IN_WATTS;
		double sleepConsumptionWh = totalSleepingDuration.minus(totalSleepTransition).asHours() * SLEEP_POWER_IN_WATTS;
		
		double energyConsumptionkWh = 
				( 		activeConsumptionWh + idleConsumptionWh + sleepConsumptionWh + 
						sleepTransitionConsumptionWh) / 1000; 
		
		summary.append(String.format("\nEnergy consumption:\t%f kWh", energyConsumptionkWh));
		
		return summary.toString();
	}
	
}