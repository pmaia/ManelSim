package simulation.beefs.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import simulation.beefs.model.Machine.Status;
import core.EventScheduler;
import core.EventSource;
import core.EventSourceMultiplexer;
import core.Time;
import core.Time.Unit;
import core.TimeInterval;

/**
 * 
 * @author Patrick Maia
 *
 */
public class MachineTransitionsFromSleepingTest {
	
	private final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
	private final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
	private final Time TEN_MINUTES = new Time(10*60, Unit.SECONDS);
	
	private Machine machine;
	
	@Before
	public void setup() {
		EventSourceMultiplexer eventsMultiplexer = new EventSourceMultiplexer(new EventSource[0]);
		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
		
		machine = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		machine.setIdle(Time.GENESIS, TO_SLEEP_TIMEOUT.plus(TEN_MINUTES));
		
		EventScheduler.start();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTransitionToIdle() {
		machine.setIdle(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.times(2)), TEN_MINUTES);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTransitionToSleep() {
		machine.setSleeping(TO_SLEEP_TIMEOUT.plus(TEN_MINUTES), TEN_MINUTES);
	}
	
	@Test
	public void testTransitionToActive() {
		machine.setActive(TO_SLEEP_TIMEOUT.plus(TEN_MINUTES), TEN_MINUTES);
		
		assertEquals(Status.TRANSITION, machine.getStatus());
		assertEquals(1, machine.getSleepIntervals().size());
		assertEquals(2, machine.getTransitionIntervals().size()); // remember that there was a transition to sleep
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		
		EventScheduler.start();
		
		assertEquals(3, EventScheduler.processCount()); // IDLE -> SLEEP, TRANSITION -> SLEEP, TRANSITION -> ACTIVE
		
		assertEquals(Status.ACTIVE, machine.getStatus());
		assertEquals(1, machine.getSleepIntervals().size());
		assertEquals(2, machine.getTransitionIntervals().size());
		assertEquals(1, machine.getUserActivityIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		
		TimeInterval expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), 
				TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION).plus(TEN_MINUTES.minus(TRANSITION_DURATION)));
		assertTrue(machine.getSleepIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT, TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION));
		assertTrue(machine.getTransitionIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TEN_MINUTES),
				TO_SLEEP_TIMEOUT.plus(TEN_MINUTES).plus(TRANSITION_DURATION));
		assertTrue(machine.getTransitionIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TEN_MINUTES).plus(TRANSITION_DURATION), 
				TO_SLEEP_TIMEOUT.plus(TEN_MINUTES).plus(TRANSITION_DURATION).plus(TEN_MINUTES));
		assertTrue(machine.getUserActivityIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(Time.GENESIS, TO_SLEEP_TIMEOUT);
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
	}
	
	@Test
	public void testWakeOnLan() {
		machine.wakeOnLan();
		
		assertEquals(Status.TRANSITION, machine.getStatus());
		assertEquals(1, machine.getSleepIntervals().size());
		assertEquals(2, machine.getTransitionIntervals().size());
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		
		EventScheduler.start();
		assertEquals(3, EventScheduler.processCount()); //IDLE -> SLEEP, TRANSITION -> SLEEP, SLEEP -> IDLE
		
		assertEquals(Status.IDLE, machine.getStatus());
		assertEquals(1, machine.getSleepIntervals().size());
		assertEquals(2, machine.getTransitionIntervals().size());
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(2, machine.getUserIdlenessIntervals().size());
		
		TimeInterval expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), 
				TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION)); // wakeOnLan is fired immediately after the machine sleep 
		assertTrue(machine.getSleepIntervals().contains(expectedInterval));
		
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT, TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION));
		assertTrue(machine.getTransitionIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), 
				TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION).plus(TRANSITION_DURATION));
		assertTrue(machine.getTransitionIntervals().contains(expectedInterval));

		expectedInterval = new TimeInterval(Time.GENESIS, TO_SLEEP_TIMEOUT);
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.times(2)),
				TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.times(2)).plus(TEN_MINUTES.minus(TRANSITION_DURATION.times(2))));
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNonContiguousTransitionToActive1() { //TimeInterval is after machine's current state interval 
		machine.setActive(TO_SLEEP_TIMEOUT.plus(TEN_MINUTES.times(2)), TEN_MINUTES);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNonContiguousTransitionToActive2() { //TimeInterval is before machine's current state interval 
		machine.setActive(Time.GENESIS, TEN_MINUTES);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNonContiguousTransitionToActive3() { //TimeInterval overlaps machine's current state interval 
		machine.setActive(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), TEN_MINUTES);
	}
}
