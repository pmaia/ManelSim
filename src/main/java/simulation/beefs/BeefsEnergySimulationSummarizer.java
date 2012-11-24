package simulation.beefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import simulation.beefs.energy.EnergyConsumptionModel;
import simulation.beefs.energy.EnergyState;
import simulation.beefs.energy.EnergyStateInterval;
import simulation.beefs.model.DataServer;
import simulation.beefs.model.Machine.MachineStateInterval;
import simulation.beefs.model.Machine.State;
import core.Context;
import core.Summarizer;
import core.TimeInterval;

/**
 * 
 * @author Patrick Maia
 *
 */
public class BeefsEnergySimulationSummarizer implements Summarizer {
	
	/*
	 * TODO add javadoc explaining what the heck this method do. add ascii arts with examples of possible combinations
	 */
	private List<EnergyStateInterval> combine(DataServer dataServer) { 
		// convert MachineStateIntervals to EnergyStateIntervals
		List<EnergyStateInterval> combinedEnergyStatesIntervals = 
				convertStates(dataServer.getHost().getStateIntervals());
		
		// combine writes
		Iterator<TimeInterval> writesIterator = dataServer.getWriteIntervals().iterator();
		combinedEnergyStatesIntervals = combine(combinedEnergyStatesIntervals.iterator(), writesIterator, false);

		// combine reads 
		Iterator<TimeInterval> readsIterator = dataServer.getReadIntervals().iterator();
		combinedEnergyStatesIntervals = combine(combinedEnergyStatesIntervals.iterator(), readsIterator, true);
		
		return combinedEnergyStatesIntervals;
	}
	
	private List<EnergyStateInterval> convertStates(List<MachineStateInterval> stateIntervals) {
		List<EnergyStateInterval> energyStatesIntervals = new ArrayList<EnergyStateInterval>();
		
		for(MachineStateInterval machineStateInterval : stateIntervals) {
			energyStatesIntervals.add(new EnergyStateInterval(convertState(machineStateInterval.getState()), 
					machineStateInterval.getInterval()));
		}
		
		return energyStatesIntervals;
	}

	private EnergyState convertState(State state) {
		EnergyState converted;
		switch(state) {
			case ACTIVE: converted = EnergyState.ACTIVE; break;
			case GOING_SLEEP: 
			case WAKING_UP: converted = EnergyState.TRANSITIONING; break;
			case IDLE: converted = EnergyState.IDLE; break;
			case SLEEPING: converted = EnergyState.SLEEPING; break;
			
			default: 
				throw new IllegalArgumentException("Could not convert " + state);
		}
		return converted;
	}
	
	private List<EnergyStateInterval> combine(Iterator<EnergyStateInterval> partiallyCombinedIntervalsIterator, 
			Iterator<TimeInterval> operationIntervalsIterator, boolean areReadIntervals) {
		
		List<EnergyStateInterval> combinedStates = new ArrayList<EnergyStateInterval>();
		
		TimeInterval nextOperationInterval = getNext(operationIntervalsIterator);
		EnergyStateInterval energyStateInterval = getNext(partiallyCombinedIntervalsIterator);
		
		while(energyStateInterval != null && nextOperationInterval != null) {
			if(!energyStateInterval.getInterval().overlaps(nextOperationInterval)) { 
				combinedStates.add(energyStateInterval);
				energyStateInterval = getNext(partiallyCombinedIntervalsIterator);
			} else {
				TimeInterval intersection = energyStateInterval.getInterval().intersection(nextOperationInterval);
				TimeInterval [] stateIntervalMinusOperationInterval = energyStateInterval.getInterval().diff(nextOperationInterval);

				EnergyState energyState = energyStateInterval.getEnergyState();
				combinedStates.add(new EnergyStateInterval(energyState, 
						stateIntervalMinusOperationInterval[0]));
				
				EnergyState newEnergyState = areReadIntervals ? energyState.addRead() : energyState.addWrite();
				combinedStates.add(new EnergyStateInterval(newEnergyState, intersection));

				if(stateIntervalMinusOperationInterval[1] != null) {
					energyStateInterval = new EnergyStateInterval(energyStateInterval.getEnergyState(), 
							stateIntervalMinusOperationInterval[1]);
					nextOperationInterval = getNext(operationIntervalsIterator);
				} else {
					nextOperationInterval = nextOperationInterval.diff(energyStateInterval.getInterval())[0];
					if(nextOperationInterval == null) {
						nextOperationInterval = getNext(operationIntervalsIterator);
					}
					energyStateInterval = getNext(partiallyCombinedIntervalsIterator);
				}
			}
		}
		
		if(energyStateInterval != null) {
			combinedStates.add(energyStateInterval);
		}
		
		return combinedStates;
	}

	private <T> T getNext(Iterator<T> iterator) {
		return iterator.hasNext() ? iterator.next() : null; 
	}

	@SuppressWarnings("unchecked")
	@Override
	public String summarize(Context context) {
		
		Set<DataServer> dataServers = (Set<DataServer>) context.get(BeefsEnergySimulationConstants.DATA_SERVERS);
		StringBuffer sb = new StringBuffer();
		
		for(DataServer dataServer : dataServers) {
			List<EnergyStateInterval> energyStatesIntervals = combine(dataServer);
			
			EnergyConsumptionModel energyConsumptionModel = 
					(EnergyConsumptionModel)context.get(BeefsEnergySimulationConstants.ENERGY_CONSUMPTION_MODEL);
			double kWh = energyConsumptionModel.getConsumption(energyStatesIntervals);
			
			sb.append(String.format("%s\n\n%f kWh\n%d transitions\n======================\n", 
					dataServer.getHost().getName(), 
					kWh, 
					dataServer.getHost().getTransitionIntervals().size()));
		}
		
		return sb.toString();
	} 

}
