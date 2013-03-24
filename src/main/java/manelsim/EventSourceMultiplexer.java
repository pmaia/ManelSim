package manelsim;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.WeakHashMap;

public class EventSourceMultiplexer {
	
	private static final int QUEUE_INITIAL_CAPACITY = 1000;
	
	private final PushBackEventSource [] eventSources;
	
	private final PriorityQueue<Event> generatedEventsQueue;
	
	private final Map<Event, Long> arrivalOrderMap = new WeakHashMap<Event, Long>();
	
	private long arrivalCount = 0;

	public EventSourceMultiplexer(EventSource[] eventSources) {
		this.eventSources = new PushBackEventSource[eventSources.length];
		
		this.generatedEventsQueue = new PriorityQueue<Event>(QUEUE_INITIAL_CAPACITY, new FIFOWhenSamePriority());
		
		for(int i = 0; i < eventSources.length; i++) {
			this.eventSources[i] = new PushBackEventSource(eventSources[i]);
		}
	}
	
	public void addNewEvent(Event event) {
		arrivalOrderMap.put(event, arrivalCount++);
		generatedEventsQueue.add(event);
	}
	
	public void removeEvent(Event event) {
		generatedEventsQueue.remove(event);
	}

	public Event getNextEvent() {
		int smallestTimeEventSourceId = 0;
		Event smallestTimeEvent = null;
		Event smallestTimeEventCandidate = null;
		
		if(eventSources.length > 0) {
			while((smallestTimeEvent = eventSources[smallestTimeEventSourceId].getNextEvent()) == null) {
				smallestTimeEventSourceId++;
				
				if(smallestTimeEventSourceId == this.eventSources.length){
					break;
				}
			}

			for(int i = smallestTimeEventSourceId + 1; i < this.eventSources.length; i++) {
				smallestTimeEventCandidate = eventSources[i].getNextEvent();
				
				if(smallestTimeEventCandidate != null){
					if(smallestTimeEvent.getScheduledTime().isEarlierThan(smallestTimeEventCandidate.getScheduledTime())) {
						eventSources[i].pushBack(smallestTimeEventCandidate);
					} else {
						eventSources[smallestTimeEventSourceId].pushBack(smallestTimeEvent);
						
						smallestTimeEventSourceId = i;
						smallestTimeEvent = smallestTimeEventCandidate;
					}
				}
			}
		}
		
		smallestTimeEventCandidate = generatedEventsQueue.poll();
		if(smallestTimeEventCandidate != null) {
			Time smallestTimeCandidate = smallestTimeEventCandidate.getScheduledTime();
			if(smallestTimeEvent == null) {
				smallestTimeEvent = smallestTimeEventCandidate;
			} else {
				Time timeOfSmallestTimeEvent = smallestTimeEvent.getScheduledTime();
				if(smallestTimeCandidate.equals(timeOfSmallestTimeEvent) || // because generated events have precedence over regular ones
						smallestTimeCandidate.isEarlierThan(timeOfSmallestTimeEvent)) {
					eventSources[smallestTimeEventSourceId].pushBack(smallestTimeEvent);
					smallestTimeEvent = smallestTimeEventCandidate;
				} else {
					generatedEventsQueue.add(smallestTimeEventCandidate);
				}
			}
		}
		
		return smallestTimeEvent;
	}
	
	/**
	 * 
	 *	Adds the ability to "push back" an event to an {@link EventSource}
	 */
	private static class PushBackEventSource implements EventSource {
		
		private final EventSource decoratedEventSource;
		private Event 	pushedBackEvent;
		
		public PushBackEventSource(EventSource parser) {
			this.decoratedEventSource = parser;
		}

		@Override
		public Event getNextEvent() {
			Event nextEvent;
			if (pushedBackEvent != null) {
				nextEvent = pushedBackEvent;
				pushedBackEvent = null;
			} else {
				nextEvent = decoratedEventSource.getNextEvent();
			}
			
			return nextEvent;
		}
		
		public void pushBack(Event event) {
			this.pushedBackEvent = event;
		}
		
	}
	
	//TODO add comment explaining why this is necessary
	private class FIFOWhenSamePriority implements Comparator<Event> {
		@Override
		public int compare(Event e1, Event e2) {
			if(e1.compareTo(e2) == 0) {
				long diff = arrivalOrderMap.get(e1) - arrivalOrderMap.get(e2);
				if (diff < 0) {
					return -1;
				} else if (diff > 0) {
					return 1;
				}
			}
			return e1.compareTo(e2);
		}
	}

}
