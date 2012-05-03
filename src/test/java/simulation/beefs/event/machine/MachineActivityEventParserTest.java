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
package simulation.beefs.event.machine;

import static org.junit.Assert.assertEquals;

import java.util.PriorityQueue;


import org.junit.Test;

import simulation.beefs.Machine;
import simulation.beefs.event.machine.MachineActivityTraceEventSource;
import simulation.beefs.util.FakeUserIdlenessTraceStream;

import core.Event;



/**
 * A suite of tests to the {@link MachineActivityTraceEventSource} class
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MachineActivityEventParserTest {
	
	@Test
	public void eventCountTest() {
		int expectedEventCount = 10;
		FakeUserIdlenessTraceStream fakeEventStream = 
			new FakeUserIdlenessTraceStream(expectedEventCount);
		
		PriorityQueue<Event> eventsGeneratedBySimulationQueue = new PriorityQueue<Event>();
		Machine machine = new Machine(eventsGeneratedBySimulationQueue, "machine", 30 * 60);
		
		MachineActivityTraceEventSource eventParser = new MachineActivityTraceEventSource(machine, fakeEventStream);
		
		int eventCount = 0;
		while(eventParser.getNextEvent() != null) {
			eventCount++;
		}
		
		assertEquals(expectedEventCount, eventCount);
	}
	
}
