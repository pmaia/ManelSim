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
package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.PriorityQueue;

import kernel.Event;
import kernel.Time;
import kernel.Time.Unit;

import org.junit.Before;
import org.junit.Test;

import emulator.event.machine.SleepEvent;
import emulator.event.machine.UserIdlenessEvent;

/**
 * A suite of tests to {@link Machine} class
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MachineTest {
	
	private Machine machine;
	private String machineId =  "id";
	private PriorityQueue<Event> eventsGeneratedBySimulationQueue;
	private long timeBeforeSleep; 
	
	@Before
	public void setup() {
		timeBeforeSleep = 30 * 60;
		eventsGeneratedBySimulationQueue = new PriorityQueue<Event>();
		machine = new Machine(eventsGeneratedBySimulationQueue, machineId, timeBeforeSleep);
		Aggregator.getInstance().reset();
	}

	@Test
	public void handleUserIdlenessStartTest() {
		UserIdlenessEvent idlenessEvent = new UserIdlenessEvent(machine, new Time(0L, Unit.SECONDS), 
				new Time(timeBeforeSleep - 10, Unit.SECONDS));
		
		machine.handleEvent(idlenessEvent);
		assertTrue(machine.isAwake()); //it should be awake...
		assertEquals(0, eventsGeneratedBySimulationQueue.size()); //and no event should be triggered
	}
	
	@Test
	public void handleUserIdlenessStartTest1() {
		UserIdlenessEvent idlenessEvent = new UserIdlenessEvent(machine, new Time(0L, Unit.SECONDS), 
				new Time(timeBeforeSleep + 10, Unit.SECONDS));
		machine.handleEvent(idlenessEvent);
		assertTrue(machine.isAwake()); // it must be awake...
		assertEquals(1, eventsGeneratedBySimulationQueue.size()); // ...and a SleepEvent be triggered
		
		Event scheduledEvent = eventsGeneratedBySimulationQueue.poll();
		assertTrue(scheduledEvent instanceof SleepEvent);
		machine.handleEvent(scheduledEvent);
		assertFalse(machine.isAwake());
		assertEquals(0, eventsGeneratedBySimulationQueue.size());
		
		MachineAvailability machineAvailability = 
			Aggregator.getInstance().getMachineAvailability(machineId);
		
		assertEquals(new Time(timeBeforeSleep, Unit.SECONDS), machineAvailability.getTotalIdleDuration());
		// because the time spent sleeping is just counted when some state transition occurs, then...
		assertEquals(new Time(0, Unit.SECONDS), machineAvailability.getTotalSleepingDuration());
		// and
		assertEquals(0, machineAvailability.getSleepCount());
	}
	
	@Test
	public void testEquals() {
		Machine machine1 = new Machine(eventsGeneratedBySimulationQueue, "id1", timeBeforeSleep);
		Machine machine2 = new Machine(eventsGeneratedBySimulationQueue, "id2", timeBeforeSleep);
		
		assertFalse(machine1.equals(machine2));
		assertTrue(machine1.equals(machine1));
		assertTrue(machine1.hashCode() != machine2.hashCode());
	}
}
