package simulation.beefs.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import simulation.beefs.event.machine.UserActivity;
import simulation.beefs.model.Machine.State;
import simulation.beefs.util.Noop;
import core.EventScheduler;
import core.EventSource;
import core.Time;
import core.Time.Unit;

/**
 * 
 * @author Patrick Maia
 *
 */
public class MachineTransitionsFromWakingUpToActiveTest extends TransitionStatesBaseTest {
	
	private Machine machineWakingUpToActive;
	
	@Before
	public void setup() {
		eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);

		machineWakingUpToActive = new Machine("cherne", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		machineWakingUpToActive.setIdle(Time.GENESIS, TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
		machineWakingUpToActive.setSleeping(TO_SLEEP_TIMEOUT, ONE_MINUTE);
		machineWakingUpToActive.setSleeping(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), 
				ONE_MINUTE.minus(TRANSITION_DURATION));
		machineWakingUpToActive.setActive(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE), ONE_MINUTE);
		assertEquals(State.WAKING_UP, machineWakingUpToActive.getState());
	}

	/*
	 * It is only possible to wake up to idle when the waking up is motivated by a wakeOnLan and there is still enough
	 * remaining sleeping time (time sufficient to make the transition without an activity event arrive).
	 */
	@Test(expected=IllegalStateException.class)
	public void testTransitionToIdle() {
		machineWakingUpToActive.setIdle(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE.plus(TRANSITION_DURATION)), ONE_MINUTE);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTransitionToSleep() {
		machineWakingUpToActive.setSleeping(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE.plus(TRANSITION_DURATION)), ONE_MINUTE);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testTransitionToActiveAfterExpectedPeriod() {
		machineWakingUpToActive.setActive(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE.plus(TRANSITION_DURATION.plus(ONE_SECOND))),
				ONE_MINUTE);
	}
	
	@Test
	public void testTransitionToActiveOnExpectedTime() {
		machineWakingUpToActive.setActive(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE.plus(TRANSITION_DURATION)), ONE_MINUTE);
		assertEquals(TRANSITION_DURATION, machineWakingUpToActive.currentDelay());
		assertEquals(State.ACTIVE, machineWakingUpToActive.getState());
	}
	
	@Test
	public void testWakeOnLan() {
		int before = eventsMultiplexer.queueSize();
		machineWakingUpToActive.wakeOnLan(); // this must be innocuous
		assertEquals(before, eventsMultiplexer.queueSize());
		assertEquals(State.WAKING_UP, machineWakingUpToActive.getState());
	}

	/* 
	 * This happens when a machine receives a wakeOnLan message just before it's about to wake up because 
	 * of a trace event.
	 */
	@Test
	public void testTransitionToActiveBeforeWakingUpEnd() {
		eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);

		machineWakingUpToActive = new Machine("cherne", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		machineWakingUpToActive.setIdle(Time.GENESIS, TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
		EventScheduler.start();
		assertEquals(State.SLEEPING, machineWakingUpToActive.getState());
		
		Time wakeOnLanTime = TO_SLEEP_TIMEOUT.plus(ONE_MINUTE.minus(ONE_SECOND.times(2))); // two seconds before the sleeping end
		
		Noop noop = new Noop(wakeOnLanTime);
		EventScheduler.schedule(noop);
		EventScheduler.start();
		machineWakingUpToActive.wakeOnLan();
		assertEquals(State.WAKING_UP, machineWakingUpToActive.getState());
		
		// setActive 0.5 seconds before the waking up end (this is the time the original sleeping would end if the wakeOnLan never happened)
		machineWakingUpToActive.setActive(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE), ONE_MINUTE); 
		assertEquals(State.WAKING_UP, machineWakingUpToActive.getState());
		UserActivity userActivity = 
			new UserActivity(machineWakingUpToActive, wakeOnLanTime.plus(TRANSITION_DURATION), ONE_MINUTE);
		assertTrue(eventsMultiplexer.contains(userActivity));
		
		EventScheduler.start();
		assertEquals(new Time(500, Unit.MILLISECONDS), machineWakingUpToActive.currentDelay());
		assertEquals(State.ACTIVE, machineWakingUpToActive.getState());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTransitionToActiveBeforeWakingUpEndTwice() {
		eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);

		machineWakingUpToActive = new Machine("cherne", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
		machineWakingUpToActive.setIdle(Time.GENESIS, TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
		EventScheduler.start();
		assertEquals(State.SLEEPING, machineWakingUpToActive.getState());
		
		Time wakeOnLanTime = TO_SLEEP_TIMEOUT.plus(ONE_MINUTE.minus(ONE_SECOND.times(2))); // two seconds before the sleeping end
		
		Noop noop = new Noop(wakeOnLanTime);
		EventScheduler.schedule(noop);
		EventScheduler.start();
		machineWakingUpToActive.wakeOnLan();
		assertEquals(State.WAKING_UP, machineWakingUpToActive.getState());
		
		// setActive 0.5 seconds before the waking up end (this is the time the original sleeping would end if the wakeOnLan never happened)
		machineWakingUpToActive.setActive(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE), ONE_MINUTE); 
		assertEquals(State.WAKING_UP, machineWakingUpToActive.getState());
		UserActivity userActivity = 
			new UserActivity(machineWakingUpToActive, wakeOnLanTime.plus(TRANSITION_DURATION), ONE_MINUTE);
		assertTrue(eventsMultiplexer.contains(userActivity));
		
		machineWakingUpToActive.setActive(TO_SLEEP_TIMEOUT.plus(ONE_MINUTE), ONE_MINUTE);
	}

}
