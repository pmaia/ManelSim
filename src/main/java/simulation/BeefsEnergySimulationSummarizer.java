package simulation;

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
	
	private List<EnergyStateInterval> combine(DataServer dataServer) { 
		List<EnergyStateInterval> combinedEnergyStatesIntervals;
		
		// combine writes
		Iterator<MachineStateInterval> stateIntervalsIterator = dataServer.getHost().getStateIntervals().iterator();
		Iterator<TimeInterval> writesIterator = dataServer.getWriteIntervals().iterator();
		combinedEnergyStatesIntervals = combineWrites(stateIntervalsIterator, writesIterator);

		// combine reads 
		Iterator<TimeInterval> readsIterator = dataServer.getReadIntervals().iterator();
		combinedEnergyStatesIntervals = combineReads(combinedEnergyStatesIntervals.iterator(), readsIterator);
		
		return combinedEnergyStatesIntervals;
	}
	
	private List<EnergyStateInterval> combineWrites(Iterator<MachineStateInterval> stateIntervalsIterator, 
			Iterator<TimeInterval> writesIterator) {
		
		TimeInterval nextWriteInterval = getNext(writesIterator);
		MachineStateInterval stateInterval = getNext(stateIntervalsIterator);
		List<EnergyStateInterval> energyStatesIntervals = new ArrayList<EnergyStateInterval>();		
		
		while(stateInterval != null && nextWriteInterval != null) {
			if(!stateInterval.getInterval().overlaps(nextWriteInterval)) { 
				energyStatesIntervals.add(new EnergyStateInterval(convertState(stateInterval.getState()), 
						stateInterval.getInterval()));
				stateInterval = getNext(stateIntervalsIterator);
			} else {
				TimeInterval intersection = stateInterval.getInterval().intersection(nextWriteInterval);
				TimeInterval [] stateIntervalMinusWriteInterval = stateInterval.getInterval().diff(nextWriteInterval);

				EnergyState convertedState = convertState(stateInterval.getState());
				energyStatesIntervals.add(new EnergyStateInterval(convertedState, 
						stateIntervalMinusWriteInterval[0]));
				energyStatesIntervals.add(new EnergyStateInterval(addWrite(convertedState), intersection));

				if(stateIntervalMinusWriteInterval[1] != null) {
					stateInterval = 
							new MachineStateInterval(stateInterval.getState(), stateIntervalMinusWriteInterval[1]);
					nextWriteInterval = getNext(writesIterator);
				} else {
					nextWriteInterval = nextWriteInterval.diff(stateInterval.getInterval())[0];
					if(nextWriteInterval == null) {
						nextWriteInterval = getNext(writesIterator);
					}
					stateInterval = getNext(stateIntervalsIterator);
				}
			}
		}
		
		if(stateInterval != null) {
			energyStatesIntervals.add(new EnergyStateInterval(convertState(stateInterval.getState()), 
					stateInterval.getInterval()));
		}
		
		return energyStatesIntervals;
	}
	
	private EnergyState addWrite(EnergyState state) {
		EnergyState statePlusRead;
		switch(state) {
		case READ_ACTIVE: statePlusRead = EnergyState.READ_WRITE_ACTIVE; break;
		case READ_IDLE: statePlusRead = EnergyState.READ_WRITE_IDLE; break;
		case ACTIVE: statePlusRead = EnergyState.WRITE_ACTIVE; break;
		case IDLE: statePlusRead = EnergyState.WRITE_IDLE; break;
		default: 
			throw new IllegalArgumentException("Could not add " + state + " and WRITE");
		}
		return statePlusRead;
	}
	
	private List<EnergyStateInterval> combineReads(Iterator<EnergyStateInterval> partiallyCombinedIntervalsIterator, 
			Iterator<TimeInterval> readsIterator) {
		
		List<EnergyStateInterval> combinedStates = new ArrayList<EnergyStateInterval>();
		
		TimeInterval nextReadInterval = getNext(readsIterator);
		EnergyStateInterval energyStateInterval = getNext(partiallyCombinedIntervalsIterator);
		
		while(energyStateInterval != null && nextReadInterval != null) {
			if(!energyStateInterval.getInterval().overlaps(nextReadInterval)) { 
				combinedStates.add(energyStateInterval);
				energyStateInterval = getNext(partiallyCombinedIntervalsIterator);
			} else {
				TimeInterval intersection = energyStateInterval.getInterval().intersection(nextReadInterval);
				TimeInterval [] stateIntervalMinusReadInterval = energyStateInterval.getInterval().diff(nextReadInterval);

				EnergyState energyState = energyStateInterval.getEnergyState();
				combinedStates.add(new EnergyStateInterval(energyState, 
						stateIntervalMinusReadInterval[0]));
				combinedStates.add(new EnergyStateInterval(addRead(energyState), intersection));

				if(stateIntervalMinusReadInterval[1] != null) {
					energyStateInterval = new EnergyStateInterval(energyStateInterval.getEnergyState(), 
							stateIntervalMinusReadInterval[1]);
					nextReadInterval = getNext(readsIterator);
				} else {
					nextReadInterval = nextReadInterval.diff(energyStateInterval.getInterval())[0];
					if(nextReadInterval == null) {
						nextReadInterval = getNext(readsIterator);
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

	private EnergyState addRead(EnergyState state) {
		EnergyState statePlusRead;
		switch(state) {
		case WRITE_ACTIVE: statePlusRead = EnergyState.READ_WRITE_ACTIVE; break;
		case WRITE_IDLE: statePlusRead = EnergyState.READ_WRITE_IDLE; break;
		case ACTIVE: statePlusRead = EnergyState.READ_ACTIVE; break;
		case IDLE: statePlusRead = EnergyState.READ_IDLE; break;
		default: 
			throw new IllegalArgumentException("Could not add " + state + " and READ");
		}
		return statePlusRead;
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
			long kWh = energyConsumptionModel.getConsumption(energyStatesIntervals);
			
			sb.append(String.format("%s\t%d kWh", dataServer.getHost().getName(), kWh));
		}
		
		return sb.toString();
	} 

}
