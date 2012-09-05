package simulation;

import java.util.List;
import java.util.Set;

import simulation.beefs.energy.EnergyConsumptionModel;
import simulation.beefs.energy.EnergyStateInterval;
import simulation.beefs.model.DataServer;
import simulation.beefs.model.Machine;
import core.Context;
import core.Summarizer;

/**
 * 
 * @author Patrick Maia
 *
 */
public class BeefsEnergySimulationSummarizer implements Summarizer {
	
	private List<EnergyStateInterval> combine(Set<DataServer> dataServers) {
		for(DataServer dataServer : dataServers) {
			Machine machine = dataServer.getHost();
//			machine.
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String summarize(Context context) {
		
		Set<DataServer> dataServers = (Set<DataServer>) context.get(BeefsEnergySimulationConstants.DATA_SERVERS);
		List<EnergyStateInterval> energyStatesIntervals = combine(dataServers);
		
		EnergyConsumptionModel energyConsumptionModel = 
				(EnergyConsumptionModel)context.get(BeefsEnergySimulationConstants.ENERGY_CONSUMPTION_MODEL);
		long kwh = energyConsumptionModel.getConsumption(energyStatesIntervals);
		
		//TODO formatar o resultado e retornar (métricas)
		return null;
	}

}
