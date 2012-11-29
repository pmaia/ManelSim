package simulation;

import java.util.List;
import java.util.Set;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.Machine.MachineStateInterval;
import core.Context;
import core.Summarizer;
import core.TimeInterval;

/**
 * @author manel
 */
public class BeefsLocalitySimulationSummarizer implements Summarizer {
	
	@Override
	public String summarize(Context context) {
		
		Set<DataServer> dataServers = (Set<DataServer>) context.get(BeefsEnergySimulationConstants.DATA_SERVERS);
		StringBuffer sb = new StringBuffer();
		
		for(DataServer ds : dataServers) {
			
			List<MachineStateInterval> stateIntervals = ds.getHost().getStateIntervals();
			List<TimeInterval> sleepIntervals = ds.getHost().getSleepIntervals();
			List<TimeInterval> transitionIntervals = ds.getHost().getTransitionIntervals();
			List<TimeInterval> userActivityIntervals = ds.getHost().getUserActivityIntervals();
			List<TimeInterval> userIdlenessIntervals = ds.getHost().getUserIdlenessIntervals();
			
			sb.append(
					String.format(
							"machine=%s\t" +
							"numStateIntervals=%d\t" +
							"numSleepIntervals=%d\t " +
							"numTransictionIntervals=%d\t" +
							"numUserActivityIntervals=%d\t" +
							"numUserIdlenessIntervals=%d\t" +
							"available=%d\t" +
							"usedSec=%d\t " +
							"total=%d\n",
							ds.getHost().getName(), 
							stateIntervals.size(),
							sleepIntervals.size(),
							transitionIntervals.size(),
							userActivityIntervals.size(),
							userIdlenessIntervals.size(),
							ds.availableSpace(),
							ds.secondaryUsedSpace(),
							ds.totalSpace()
							)
						);
		}
		
		Set<FileSystemClient> clients = (Set<FileSystemClient>) context.get(BeefsLocalitySimulationConstants.CLIENTS);
		for (FileSystemClient client : clients) {
			sb.append(
					String.format(
							"readsWhileClientSleeping=%d\t" +
							"readsWhileDataServerSleeping=%d\t" +
							"writesWhileClientSleeping=%d\t " +
							"writesWhileDataServerSleeping=%d\n",
							client.readsWhileClientSleeping(),
							client.readsWhileDataServerSleeping(),
							client.writesWhileClientSleeping(),
							client.writesWhileDataServerSleeping()
							)
						);
		}
		
		return sb.toString();
	} 

}
