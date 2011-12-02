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
package ddg.emulator;

import ddg.kernel.Event;

/**
 * An {@link EventSource} that aggregates a bunch of {@link EventSource}s and delivery their events in order.  
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MultipleEventSource implements EventSource {
	
	private final PushBackEventParser [] parsers;

	public MultipleEventSource(EventSource[] parsers) {
		this.parsers = new PushBackEventParser[parsers.length];
		
		for(int i = 0; i < parsers.length; i++) {
			this.parsers[i] = new PushBackEventParser(parsers[i]);
		}
	}

	@Override
	public Event getNextEvent() {
		
		int smallestTimeEventSourceId = 0;
		Event smallestTimeEvent;
		while((smallestTimeEvent = parsers[smallestTimeEventSourceId].getNextEvent()) == null) {
			smallestTimeEventSourceId++;
			
			if(smallestTimeEventSourceId == this.parsers.length){
				break;
			}
		}

		for(int i = smallestTimeEventSourceId + 1; i < this.parsers.length; i++) {
			Event smallestTimeEventCandidate = parsers[i].getNextEvent();
			
			if(smallestTimeEventCandidate != null){
				
				if(smallestTimeEvent.getScheduledTime().isEarlierThan(smallestTimeEventCandidate.getScheduledTime())) {
					parsers[i].pushBack(smallestTimeEventCandidate);
				} else {
					parsers[smallestTimeEventSourceId].pushBack(smallestTimeEvent);
					
					smallestTimeEventSourceId = i;
					smallestTimeEvent = smallestTimeEventCandidate;
				}
				
			}
			
		}
		
		return smallestTimeEvent;
	}
	
	/**
	 * 
	 *	Adds the ability to "push back" an event to an {@link EventSource}
	 */
	private static class PushBackEventParser implements EventSource {
		
		private final EventSource decoratedEventParser;
		private Event 	pushedBackEvent;
		
		public PushBackEventParser(EventSource parser) {
			this.decoratedEventParser = parser;
		}

		@Override
		public Event getNextEvent() {
			Event nextEvent;
			if (pushedBackEvent != null) {
				nextEvent = pushedBackEvent;
				pushedBackEvent = null;
			} else {
				nextEvent = decoratedEventParser.getNextEvent();
			}
			
			return nextEvent;
		}
		
		public void pushBack(Event event) {
			this.pushedBackEvent = event;
		}
		
	}

}
