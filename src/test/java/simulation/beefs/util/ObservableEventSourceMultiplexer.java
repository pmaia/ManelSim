package simulation.beefs.util;

import java.util.ArrayList;
import java.util.List;

import core.Event;
import core.EventSource;
import core.EventSourceMultiplexer;

public class ObservableEventSourceMultiplexer extends EventSourceMultiplexer {
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
