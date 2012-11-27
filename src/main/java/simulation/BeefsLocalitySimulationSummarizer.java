package simulation;

import java.util.List;
import java.util.Set;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.Machine.MachineStateInterval;
import core.Context;
import core.Summarizer;

/**
 * @author manel
 */
public class BeefsLocalitySimulationSummarizer implements Summarizer {
	
	
	@Override
	public String summarize(Context context) {
		
		Set<DataServer> dataServers = (Set<DataServer>) context.get(BeefsEnergySimulationConstants.DATA_SERVERS);
		StringBuffer sb = new StringBuffer();
		
		for(DataServer dataServer : dataServers) {
			
			List<MachineStateInterval> intervals = dataServer.getHost().getStateIntervals();
			
			sb.append(String.format("machine=%s\tfirst_state=%s\ttransitions=%d\n======================\n", 
					dataServer.getHost().getName(), intervals.get(0).toString(), intervals.size()));
		}
		
		return sb.toString();
	} 

}
