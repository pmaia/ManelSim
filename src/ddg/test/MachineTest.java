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
package ddg.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ddg.emulator.event.machine.Sleep;
import ddg.emulator.event.machine.UserIdlenessStart;
import ddg.emulator.event.machine.WakeUp;
import ddg.kernel.EventScheduler;
import ddg.kernel.Time;
import ddg.model.Aggregator;
import ddg.model.Machine;
import ddg.model.MachineAvailability;

/**
 * A suite of tests to {@link Machine} class
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class MachineTest {
	
	private Machine machine;
	private String machineId =  "id";
	private EventScheduler scheduler;
	private long timeBeforeSleep; 
	
	@Before
	public void setup() {
		timeBeforeSleep = 30 * 60;
		scheduler = new EventScheduler();
		machine = new Machine(scheduler, machineId, timeBeforeSleep);
		Aggregator.getInstance().reset();
	}

	@Test
	public void handleUserIdlenessStartTest() {
		UserIdlenessStart idlenessEvent = new UserIdlenessStart(machine, new Time(0L), 10);
		
		scheduler.schedule(idlenessEvent);
		scheduler.start();
		assertFalse(machine.isSleeping());
		assertEquals(0, scheduler.now().asMilliseconds());
		
		idlenessEvent = new UserIdlenessStart(machine, new Time(0L), timeBeforeSleep + 10);
		scheduler.schedule(idlenessEvent);
		scheduler.start();
		assertFalse(machine.isSleeping()); // because a WakeUp event is automatically scheduled
		assertEquals((timeBeforeSleep + 10) * 1000, scheduler.now().asMilliseconds()); // time advances because of the scheduled WakeUp event
		
		MachineAvailability machineAvailability = 
			Aggregator.getInstance().getMachineAvailability(machineId);
		
		assertEquals(timeBeforeSleep * 1000, machineAvailability.getActiveDurationTotal());
		assertEquals(10 * 1000, machineAvailability.getSleepingDurationTotal());
		assertEquals(2, machineAvailability.getTransitionsCount());
	}
	
	@Test
	public void handleSleepTest() {
		Sleep sleepEvent = new Sleep(machine, new Time(0L));
		
		scheduler.schedule(sleepEvent);
		scheduler.start();
		assertTrue(machine.isSleeping());
		assertEquals(0, scheduler.now().asMilliseconds());
		
		MachineAvailability machineAvailability = 
			Aggregator.getInstance().getMachineAvailability(machineId);
		
		assertEquals(0, machineAvailability.getActiveDurationTotal());
		assertEquals(0, machineAvailability.getSleepingDurationTotal());
		assertEquals(1, machineAvailability.getTransitionsCount());
	}
	
	@Test
	public void handleSleepTest2() {
		Sleep sleepEvent = new Sleep(machine, new Time(0L));
		Sleep sleepEvent2 = new Sleep(machine, new Time(10L));
		
		scheduler.schedule(sleepEvent);
		scheduler.schedule(sleepEvent2);
		scheduler.start();
		assertTrue(machine.isSleeping());
		assertEquals(10, scheduler.now().asMilliseconds());
		
		MachineAvailability machineAvailability = 
			Aggregator.getInstance().getMachineAvailability(machineId);
		
		assertEquals(0, machineAvailability.getActiveDurationTotal()); //machine is sleeping since time 0
		assertEquals(0, machineAvailability.getSleepingDurationTotal()); //because there is no event after the first sleep to advance the time
		assertEquals(1, machineAvailability.getTransitionsCount());
	}
	
	@Test
	public void handleWakeUpTest() {
		Sleep sleepEvent = new Sleep(machine, new Time(0L));
		WakeUp wakeUpEvent = new WakeUp(machine, new Time(10L), false);
		
		scheduler.schedule(sleepEvent);
		scheduler.schedule(wakeUpEvent);
		scheduler.start();
		assertFalse(machine.isSleeping());
		assertEquals(10, scheduler.now().asMilliseconds());
		
		MachineAvailability machineAvailability = 
			Aggregator.getInstance().getMachineAvailability(machineId);
		
		assertEquals(0, machineAvailability.getActiveDurationTotal());
		assertEquals(10, machineAvailability.getSleepingDurationTotal());
		assertEquals(2, machineAvailability.getTransitionsCount());
	}
	
	@Test
	public void handleWakeUpTest2() {
		UserIdlenessStart userIdlenessStart = 
			new UserIdlenessStart(machine, new Time(0L), timeBeforeSleep * 3);
		WakeUp wakeUpEvent = 
			new WakeUp(machine, new Time((timeBeforeSleep + 10) * 1000), true);
		
		scheduler.schedule(userIdlenessStart);
		scheduler.schedule(wakeUpEvent);
		scheduler.start();
		assertFalse(machine.isSleeping());
		assertEquals(timeBeforeSleep * 3000, scheduler.now().asMilliseconds());
		
		MachineAvailability machineAvailability = 
			Aggregator.getInstance().getMachineAvailability(machineId);
		
		assertEquals(4, machineAvailability.getTransitionsCount());
		
		long expectedActiveDuration = 2 * timeBeforeSleep * 1000;
		assertEquals(expectedActiveDuration, machineAvailability.getActiveDurationTotal());
		
		long expectedSleepingDuration = timeBeforeSleep * 1000;
		assertEquals(expectedSleepingDuration, machineAvailability.getSleepingDurationTotal());
	}
}
