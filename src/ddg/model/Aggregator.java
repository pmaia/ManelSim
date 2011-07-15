package ddg.model;

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
	
	public void reportTransitionToActive(String machine, long activeDuration) {
		getMachineAvailability(machine).addActiveDuration(activeDuration);
	}
	
	public void reportTransitionToInactive(String machine, long inactiveDuration) {
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
	
	public String summarizePerturbation() {
		StringBuilder summary = new StringBuilder();
		
		summary.append("\n\n============================================ \nPerturbation summary: \n");
		
		long totalPerturbationDuration = 0;
		long totalPerturbationCount = 0;
		
		for(String machine : perturbationTotalsPerMachine.keySet()) {
			MachinePerturbation mPerturbation = perturbationTotalsPerMachine.get(machine);
			
			summary.append(String.format("\nMachine=%s\tDuration=%d\tCount=%d", 
					machine, mPerturbation.getDurationTotal(), mPerturbation.getCount()));
			totalPerturbationDuration += mPerturbation.getDurationTotal();
			totalPerturbationCount += mPerturbation.getCount();
		}

		summary.append("\n\nTotal perturbation duration:\t " + totalPerturbationDuration);
		summary.append("\nTotal perturbation count:\t" + totalPerturbationCount);
		
		return summary.toString();
	}
	
	public String summarizeAvailability() {
		StringBuilder summary = new StringBuilder();
		
		summary.append("\n\n============================================ \nAvailability summary: \n");
		
		long totalActiveDuration = 0;
		long totalInactiveDuration = 0;
		long totalTransitions = 0;
		
		for(String machine : availabilityTotalsPerMachine.keySet()) {
			MachineAvailability mAvailability = availabilityTotalsPerMachine.get(machine);
			
			summary.append(String.format("\nMachine=%s\tActive (ms)=%d\tInactive (ms)=%d\tTransitions=%d", 
					mAvailability));
			
			totalActiveDuration += mAvailability.getActiveDurationTotal();
			totalInactiveDuration += mAvailability.getInactiveDurationTotal();
			totalTransitions += mAvailability.getTransitionsCount();
		}
		
		summary.append("\n\nTotal active duration:\t " + totalActiveDuration);
		summary.append("\nTotal inactive duration:\t" + totalInactiveDuration);
		summary.append("\nTotal transitions:\t" + totalTransitions);
		
		return summary.toString();
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