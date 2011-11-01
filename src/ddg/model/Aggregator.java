package ddg.model;

import static ddg.model.Machine.ACTIVE_POWER_IN_WATTS;
import static ddg.model.Machine.STAND_BY_POWER_IN_WATTS;
import static ddg.model.Machine.TRANSITION_DURATION_IN_MILLISECONDS;
import static ddg.model.Machine.TRANSITION_POWER_IN_WATTS;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ddg.model.data.DataServer;

public class Aggregator {

	private List<String> loginlogs = new LinkedList<String>();
	private final List<String> generalLogs = new LinkedList<String>();
	
	private final Map<String, MachinePerturbation> perturbationTotalsPerMachine = new HashMap<String, MachinePerturbation>();
	private final Map<String, MachineAvailability> availabilityTotalsPerMachine = new HashMap<String, MachineAvailability>();

	private static Aggregator instance = new Aggregator();

	public static Aggregator getInstance() {
		return instance;
	}

	private Aggregator() { /* empty */ }
	
	public void reportPerturbation(String machine, long duration) {
		MachinePerturbation machinePerturbation = perturbationTotalsPerMachine.get(machine);
		if(machinePerturbation == null) {
			machinePerturbation = new MachinePerturbation();
			perturbationTotalsPerMachine.put(machine, machinePerturbation);
		}
		
		machinePerturbation.addNewPerturbation(duration);
	}
	
	public void aggregateActiveDuration(String machine, long activeDuration) {
		getMachineAvailability(machine).addActiveDuration(activeDuration);
	}
	
	public void aggregateSleepingDuration(String machine, long inactiveDuration) {
		getMachineAvailability(machine).addInactiveDuration(inactiveDuration);
	}
	
	private MachineAvailability getMachineAvailability(String machine) {
		MachineAvailability machineAvailability = availabilityTotalsPerMachine.get(machine);
		if(machineAvailability == null) {
			machineAvailability = new MachineAvailability();
			availabilityTotalsPerMachine.put(machine, machineAvailability);
		}
		
		return machineAvailability;
	}
	
	public String summarize() {
		StringBuilder summary = new StringBuilder();
		
		//summarize perturbation
		summary.append("\n\n============================================ \nPerturbation summary: \n");
		
		long totalPerturbationDuration = 0;
		long totalPerturbationCount = 0;
		
		for(String machine : perturbationTotalsPerMachine.keySet()) {
			MachinePerturbation mPerturbation = perturbationTotalsPerMachine.get(machine);
			
			summary.append(String.format("\nMachine=%s\tDuration=%d ms\tCount=%d", 
					machine, mPerturbation.getDurationTotal(), mPerturbation.getCount()));
			totalPerturbationDuration += mPerturbation.getDurationTotal();
			totalPerturbationCount += mPerturbation.getCount();
		}

		summary.append(String.format("\n\nTotal perturbation duration:\t%d ms", totalPerturbationDuration));
		summary.append(String.format("\nTotal perturbation count:\t %d", totalPerturbationCount));

		//summarize availability
		summary.append("\n\n============================================ \nAvailability summary: \n");
		
		long totalActiveDurationMillis = 0;
		long totalInactiveDurationMillis = 0;
		long totalTransitions = 0;
		
		for(String machine : availabilityTotalsPerMachine.keySet()) {
			MachineAvailability mAvailability = availabilityTotalsPerMachine.get(machine);
			
			summary.append(String.format("\nMachine=%s\tActive=%d ms\tInactive=%d ms\tTransitions=%d", 
					machine, mAvailability.getActiveDurationTotal(), mAvailability.getInactiveDurationTotal(), 
					mAvailability.getTransitionsCount()));
			
			totalActiveDurationMillis += mAvailability.getActiveDurationTotal();
			totalInactiveDurationMillis += mAvailability.getInactiveDurationTotal();
			totalTransitions += mAvailability.getTransitionsCount();
		}
		
		summary.append(String.format("\n\nTotal active duration:\t%d ms", totalActiveDurationMillis));
		summary.append(String.format("\nTotal inactive duration:\t%d ms", totalInactiveDurationMillis));
		summary.append(String.format("\nTotal transitions:\t%d", totalTransitions));
		
		//summarize energy consumption
		summary.append("\n\n============================================ \nEnergy consumption summary: \n");
		double activeConsumptionWh = toHours(totalActiveDurationMillis) * ACTIVE_POWER_IN_WATTS;
		double inactiveConsumptionWh = toHours(totalInactiveDurationMillis) * STAND_BY_POWER_IN_WATTS;
		double transitionConsumptionWh = 
			totalTransitions * toHours(TRANSITION_DURATION_IN_MILLISECONDS) * TRANSITION_POWER_IN_WATTS;
		
		double energyConsumptionkWh = (activeConsumptionWh + inactiveConsumptionWh + transitionConsumptionWh) / 1000; 
		
		summary.append(String.format("\nEnergy consumption without opportunistic distributed file system:\t%f kWh",
				energyConsumptionkWh));
		
		double perturbationConsumptionkWh = 
			totalPerturbationCount * toHours(TRANSITION_DURATION_IN_MILLISECONDS) * TRANSITION_POWER_IN_WATTS;
		perturbationConsumptionkWh += (toHours(totalPerturbationDuration) * ACTIVE_POWER_IN_WATTS);
		
		double energyConsumptionDOFSkWh = perturbationConsumptionkWh + energyConsumptionkWh;
		
		summary.append(String.format("\nEnergy consumption with opportunistic distributed file system:\t%f kWh",
				energyConsumptionDOFSkWh));

		return summary.toString();
	}
	
	private double toHours(long timeInMillis) {
		return ((timeInMillis / 1000) / 60) / 60;
	}

	public void reportlogin(DDGClient client, long now) {
		DataServer dataServer = client.getMachine().getDeployedDataServers()
				.get(0);
		loginlogs.add(dataServer + "\t" + dataServer.getAvailableDiskSize()
				+ "\t" + now);
	}

	public void report(String logline) {
		generalLogs.add(logline);
	}
	
	private class MachinePerturbation {
		private long totalDuration = 0;
		private long count = 0;
		
		public void addNewPerturbation(long duration) {
			count++;
			this.totalDuration+= duration;
		}
		
		public long getDurationTotal() {
			return totalDuration;
		}
		
		public long getCount() {
			return count;
		}
	}
	
	private class MachineAvailability {
		private long activeDurationTotal = 0;
		private long inactiveDurationTotal = 0;
		private long transitionsCount = 0;
		
		public void addActiveDuration(long activeIncrement) {
			transitionsCount++;
			activeDurationTotal += activeIncrement;
		}
		
		public void addInactiveDuration(long inactiveIncrement) {
			transitionsCount++;
			inactiveDurationTotal += inactiveIncrement;
		}
		
		public long getActiveDurationTotal() {
			return activeDurationTotal;
		}
		
		public long getInactiveDurationTotal() {
			return inactiveDurationTotal;
		}
		
		public long getTransitionsCount() {
			return transitionsCount;
		}
	}
}