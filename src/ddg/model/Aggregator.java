package ddg.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ddg.model.data.DataServer;

/**
 * Information aggregator and filter.
 * 
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class Aggregator {

	private List<String> loginlogs = new LinkedList<String>();
	private final List<String> generalLogs = new LinkedList<String>();
	
	private long totalIdleUtilization = 0;
	private final Map<String, Long> idleUtilizationPerMachine = new HashMap<String, Long>();

	private static Aggregator instance = new Aggregator();

	public static Aggregator getInstance() {
		return instance;
	}

	private Aggregator() { /* empty */ }
	
	public void reportIdleUtilization(String machine, long duration) {
		totalIdleUtilization += duration;
		
		Long machineIdleUtilization = idleUtilizationPerMachine.get(machine);
		if(machineIdleUtilization == null)
			idleUtilizationPerMachine.put(machine, duration);
		else
			idleUtilizationPerMachine.put(machine, machineIdleUtilization + duration);
	}
	
	public String summarizeIdleUtilization() {
		StringBuilder summary = new StringBuilder();
		
		summary.append("Total idle utilization:\t " + totalIdleUtilization);
		
		for(String machine : idleUtilizationPerMachine.keySet())
			summary.append(String.format("\n%s:\t%d", machine, idleUtilizationPerMachine.get(machine)));
		
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

}