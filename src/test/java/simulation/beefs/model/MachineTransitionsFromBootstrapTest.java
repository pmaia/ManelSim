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
 * @author Patrick Maia
 */
public class MachineTransitionsFromBootstrapTest {
	
	private Machine machine;
	
	private Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS); //fifteen minutes
	private Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);

	@Before
	public void setup() {
		machine = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		EventSourceMultiplexer eventsMultiplexer = new EventSourceMultiplexer(new EventSource[0]);
		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
	}
	
	//test transitions from bootstrap
	
	@Test(expected=IllegalStateException.class)
	public void testTransitionFromBootstrapToActive() {
		machine.setActive(Time.GENESIS, new Time(5, Unit.SECONDS));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTransitionsFromBootstrapToSleeping() {
		machine.setSleeping(Time.GENESIS, new Time(5, Unit.SECONDS));
	}
	
	@Test
	public void testTransitionFromBootstrapToIdle1() { // idleness duration is less than TO_SLEEP_TIMEOUT
		Time idlenessDuration = new Time(10*60, Unit.SECONDS);
		machine.setIdle(Time.GENESIS, idlenessDuration );

		assertEquals(Status.IDLE, machine.getStatus());
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(0, machine.getSleepIntervals().size());
		assertEquals(0, machine.getTransitionIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		TimeInterval expectedInterval = new TimeInterval(Time.GENESIS, idlenessDuration);
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
	}
	
	@Test
	public void testTransitionFromBootstrapToIdle2() { //idleness duration is greater than TO_SLEEP_TIMEOUT
		Time idlenessDuration = new Time(20*60, Unit.SECONDS);
		machine.setIdle(Time.GENESIS, idlenessDuration);

		assertEquals(Status.IDLE, machine.getStatus());
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(0, machine.getSleepIntervals().size());
		assertEquals(0, machine.getTransitionIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		TimeInterval expectedInterval = new TimeInterval(Time.GENESIS, TO_SLEEP_TIMEOUT);
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
		
		EventScheduler.start();
		
		assertEquals(2, EventScheduler.processCount());
		assertEquals(Status.SLEEPING, machine.getStatus());
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(1, machine.getSleepIntervals().size());
		assertEquals(1, machine.getTransitionIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		
		expectedInterval = new TimeInterval(Time.GENESIS, TO_SLEEP_TIMEOUT);
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT, TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION));
		assertTrue(machine.getTransitionIntervals().contains(expectedInterval));
		//must sleep up to the end of the original idlenessDuration (20 minutes)
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), idlenessDuration); 
		assertTrue(machine.getSleepIntervals().contains(expectedInterval));
	}
	
	@Test
	public void testTransitionFromBootstrapToIdle3() { 
	/* Idleness duration is slightly greater than TO_SLEEP_TIMEOUT. 
	 * So slightly, that the remaining time after TO_SLEEP_TIMEOUT is less than TRANSITION_DURATION.
	 * This means that the idle period doesn't encompass the transition time. 
	 */
		Time idlenessDuration = TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.minus(new Time(1, Unit.MICROSECONDS)));
		machine.setIdle(Time.GENESIS, idlenessDuration);
		
		assertEquals(Status.IDLE, machine.getStatus());
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(0, machine.getSleepIntervals().size());
		assertEquals(0, machine.getTransitionIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		TimeInterval expectedInterval = new TimeInterval(Time.GENESIS, TO_SLEEP_TIMEOUT);
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
		
		EventScheduler.start();
		
		assertEquals(2, EventScheduler.processCount());
		assertEquals(Status.SLEEPING, machine.getStatus());
		assertEquals(0, machine.getUserActivityIntervals().size());
		assertEquals(1, machine.getSleepIntervals().size());
		assertEquals(1, machine.getTransitionIntervals().size());
		assertEquals(1, machine.getUserIdlenessIntervals().size());
		
		expectedInterval = new TimeInterval(Time.GENESIS, TO_SLEEP_TIMEOUT);
		assertTrue(machine.getUserIdlenessIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT, TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION));
		assertTrue(machine.getTransitionIntervals().contains(expectedInterval));
		expectedInterval = new TimeInterval(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), 
				TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION));
		assertTrue(machine.getSleepIntervals().contains(expectedInterval));
	}
}
