package manelsim;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.WeakHashMap;

public class EventSourceMultiplexer {
	
	private final PushBackEventSource [] eventSources;
	
	private final PriorityQueue<Event> generatedEventsQueue;
	
	private final Map<Event, Long> arrivalOrderMap = new WeakHashMap<Event, Long>();
	
	private long arrivalCount = 0;

	public EventSourceMultiplexer(EventSource[] eventSources) {
		this.eventSources = new PushBackEventSource[eventSources.length];
		
		this.generatedEventsQueue = new PriorityQueue<Event>();
		
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
		Event nextEvent = null;
		Event nextEventCandidate = null;
		
		if(eventSources.length > 0) {
			while((nextEvent = eventSources[smallestTimeEventSourceId].getNextEvent()) == null) {
				smallestTimeEventSourceId++;
				
				if(smallestTimeEventSourceId == this.eventSources.length){
					break;
				}
			}

			for(int i = smallestTimeEventSourceId + 1; i < this.eventSources.length; i++) {
				nextEventCandidate = eventSources[i].getNextEvent();
				
				if(nextEventCandidate != null){
					if(nextEvent.getScheduledTime().isEarlierThan(nextEventCandidate.getScheduledTime())) {
						eventSources[i].pushBack(nextEventCandidate);
					} else {
						eventSources[smallestTimeEventSourceId].pushBack(nextEvent);
						
						smallestTimeEventSourceId = i;
						nextEvent = nextEventCandidate;
					}
				}
			}
		}
		
		nextEventCandidate = generatedEventsQueue.poll();
		if(nextEventCandidate != null) {
			Time nextEventCandidateTime = nextEventCandidate.getScheduledTime();
			if(nextEvent == null) {
				nextEvent = nextEventCandidate;
			} else {
				Time nextEventTime = nextEvent.getScheduledTime();
				if(nextEventCandidateTime.equals(nextEventTime) || // because generated events have precedence over regular ones
						nextEventCandidateTime.isEarlierThan(nextEventTime)) {
					eventSources[smallestTimeEventSourceId].pushBack(nextEvent);
					nextEvent = nextEventCandidate;
				} else {
					generatedEventsQueue.add(nextEventCandidate);
				}
			}
		}
		
		return nextEvent;
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

}
