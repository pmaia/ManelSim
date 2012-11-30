/**
 * Copyright (C) 2009 Universidade Federal de Campina Grande
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package core;

import java.util.PriorityQueue;


/**
 * Aggregates a bunch of {@link EventSource}s and delivery their events in order.  
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class EventSourceMultiplexer {
	
	//FIXME: shouldn't be a Event source type ?
	
	private final PushBackEventSource [] eventSources;
	
	private final PriorityQueue<Event> generatedEventsQueue;

	public EventSourceMultiplexer(EventSource[] eventSources) {
		this.eventSources = new PushBackEventSource[eventSources.length];
		
		this.generatedEventsQueue = new PriorityQueue<Event>();
		
		for(int i = 0; i < eventSources.length; i++) {
			this.eventSources[i] = new PushBackEventSource(eventSources[i]);
		}
	}
	
	public void addNewEvent(Event event) {
		generatedEventsQueue.add(event);
	}

	public Event getNextEvent() {
		
		//FIXME: I don't understand this code
		int smallestTimeEventSourceId = 0;
		Event smallestTimeEvent = null;
		Event smallestTimeEventCandidate = null;
		
		if(eventSources.length > 0) {
			
			//It seems this loop finds the index of the first source with a non-null event (and it assign it
			//to the smallestTimeEventSourceId variable)
			//it also assigns the time of this non-null event to the smallestTimeEvent variable
			while((smallestTimeEvent = eventSources[smallestTimeEventSourceId].getNextEvent()) == null) {
				smallestTimeEventSourceId++;
				
				if(smallestTimeEventSourceId == this.eventSources.length){
					break;
				}
			}

			//this loop transverse the source array from smallestTimeEventSourceId
			//search for a non-null event earlier than the smallestTimeEvent
			//FIXME: i actually didn't understand why not making a single pass (it's likely
			//because getNextEvent() implies in push them back if it's not the earliest)
			//FIXME: that's a reason to add a peak() method ? (i'm not sure if it possible
			//in this particular case)
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
		
		//FIXME: code below is not sexy. I'm wondering if it's possible to handle
		//this generatedEvents at the same above loop
		//One option is to add them to a generatedEventsSource and add this new source
		//to eventSources array.
		smallestTimeEventCandidate = generatedEventsQueue.poll();
		if(smallestTimeEventCandidate != null) {
			Time smallestTimeCandidate = smallestTimeEventCandidate.getScheduledTime();
			if(smallestTimeEvent == null) {
				smallestTimeEvent = smallestTimeEventCandidate;
			} else if(smallestTimeCandidate.isEarlierThan(smallestTimeEvent.getScheduledTime())) {
				eventSources[smallestTimeEventSourceId].pushBack(smallestTimeEvent);
				smallestTimeEvent = smallestTimeEventCandidate;
			} else {
				generatedEventsQueue.add(smallestTimeEventCandidate);
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

}
