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
public class MachineTest {
	
	private Machine machine;
	
	private Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS); //fifteen minutes

	@Before
	public void setup() {
		machine = new Machine("jurupoca", TO_SLEEP_TIMEOUT, new Time(2500, Unit.MILLISECONDS));
		EventSourceMultiplexer eventsMultiplexer = new EventSourceMultiplexer(new EventSource[0]);
		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
	}
	
	//test transitions from bootstrap
	
	@Test(expected=IllegalStateException.class)
	public void testInvalidTransitionsFromBootstrap1() {
		machine.setActive(Time.GENESIS, new Time(5, Unit.SECONDS));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testInvalidTransitionsFromBootstrap2() {
		machine.setSleeping(Time.GENESIS, new Time(5, Unit.SECONDS));
	}
	
	@Test
	public void testTransitionFromBootstrapToIdle1() { // idleness duration is less than toSleepTimeout
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
	public void testTransitionFromBootstrapToIdle2() { //idleness duration is greater than toSleepTimeout
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
		
		//TODO continuar daqui. verificar intervalos de cada transição
	}
	
	// not contiguous transition
	// how do the trace and simulation generated events interact? 
}
