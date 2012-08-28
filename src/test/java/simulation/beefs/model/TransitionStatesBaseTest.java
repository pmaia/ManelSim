package simulation.beefs.model;

import java.util.ArrayList;
import java.util.List;

import core.Event;
import core.EventSource;
import core.EventSourceMultiplexer;
import core.Time;
import core.Time.Unit;

/**
 * 
 * @author Patrick Maia
 *
 */
public class TransitionStatesBaseTest {
	protected final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
	protected final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
	protected final Time ONE_MINUTE = new Time(60, Unit.SECONDS);
	protected final Time ONE_SECOND = new Time(1, Unit.SECONDS);

	protected ObservableEventSourceMultiplexer eventsMultiplexer;
	
	protected static class ObservableEventSourceMultiplexer extends EventSourceMultiplexer {
		private List<Event> eventsList = new ArrayList<Event>();

		public ObservableEventSourceMultiplexer(EventSource[] eventSources) {
			super(eventSources);
		}
		@Override
		public void addNewEvent(Event event) {
			eventsList.add(event);
			super.addNewEvent(event);
		}
		public boolean contains(Event event) {
			return eventsList.contains(event);
		}
		public int queueSize() {
			return eventsList.size();
		}
		public int howManyOf(Event event) {
			int count = 0;
			for(Event anEvent : eventsList) {
				if(anEvent.equals(event)) {
					count++;
				}
			}
			return count;
		}
	}
}
