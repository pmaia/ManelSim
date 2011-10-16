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

import ddg.kernel.JEEvent;

/**
 * TODO make doc
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MultipleSourceEventParser implements EventParser {
	
	private final PushBackEventParser [] parsers;

	public MultipleSourceEventParser(EventParser[] parsers) {
		this.parsers = new PushBackEventParser[parsers.length];
		
		for(int i = 0; i < parsers.length; i++) {
			this.parsers[i] = new PushBackEventParser(parsers[i]);
		}
	}

	@Override
	public JEEvent getNextEvent() {
		int smallestTimeEventParserId = 0;
		JEEvent smallestTimeEvent = 
			parsers[smallestTimeEventParserId].getNextEvent();

		for(int i = 1; i < this.parsers.length; i++ ) {
			JEEvent smallestTimeEventCandidate = parsers[i].getNextEvent();
			
			if(smallestTimeEvent.getTheScheduledTime().compareTo(
					smallestTimeEventCandidate.getTheScheduledTime()) > 0 ) {
				
				parsers[smallestTimeEventParserId].pushBack(smallestTimeEvent);
				
				smallestTimeEventParserId = i;
				smallestTimeEvent = smallestTimeEventCandidate;
			}
		}
		
		return smallestTimeEvent;
	}
	
	/**
	 * 
	 *	Adds the ability to "push back" an event to an {@link EventParser}
	 */
	private class PushBackEventParser implements EventParser {
		
		private final EventParser decoratedEventParser;
		private JEEvent 	pushedBackEvent;
		
		public PushBackEventParser(EventParser parser) {
			this.decoratedEventParser = parser;
		}

		@Override
		public JEEvent getNextEvent() {
			JEEvent nextEvent;
			if (pushedBackEvent != null) {
				nextEvent = pushedBackEvent;
				pushedBackEvent = null;
			} else {
				nextEvent = decoratedEventParser.getNextEvent();
			}
			
			return nextEvent;
		}
		
		public void pushBack(JEEvent event) {
			this.pushedBackEvent = event;
		}
		
	}

}
