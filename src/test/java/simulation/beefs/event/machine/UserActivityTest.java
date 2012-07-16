package simulation.beefs.event.machine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import simulation.beefs.model.Machine;
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
public class UserActivityTest {
	
	private Machine machine;
		
	@Before
	public void setup() {
		machine = new Machine("jurupoca", new Time(5 * 60, Unit.SECONDS), new Time(2500, Unit.MILLISECONDS));
		machine.setIdle(Time.GENESIS, new Time(4, Unit.SECONDS));		
	}

	/*
	 * Before a machine can change to any state it must pass by the idle state. 
	 */
	@Test(expected=IllegalStateException.class)
	public void testTransitionFromBootstrap() {
		Machine machine = new Machine("jurupoca", new Time(5 * 60, Unit.SECONDS), new Time(2500, Unit.MILLISECONDS));
		
		Time userActivityStart = Time.GENESIS;
		Time duration = new Time(5, Unit.SECONDS);
		
		UserActivity userActivity = new UserActivity(machine, userActivityStart, duration);
		userActivity.process();
	}
	
	@Test
	public void testTransitionFromIdle() {
		Time userActivityStart = new Time(5, Unit.SECONDS);
		Time duration = new Time(5, Unit.SECONDS);
		
		UserActivity userActivity = new UserActivity(machine, userActivityStart, duration);
		userActivity.process();
		
		List<TimeInterval> userActivityIntervals = machine.getUserActivityIntervals();
		assertEquals(1, userActivityIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(userActivityStart, userActivityStart.plus(duration));
		assertTrue(userActivityIntervals.contains(expectedTimeInterval));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTransitionFromInUse() {
		Time userActivityStart = new Time(5, Unit.SECONDS);
		Time duration = new Time(5, Unit.SECONDS);
		
		Time oneSecond = new Time(1, Unit.SECONDS);
		UserActivity userActivity = new UserActivity(machine, userActivityStart, duration);
		userActivity.process();
		userActivity = new UserActivity(machine, userActivityStart.plus(duration).plus(oneSecond), duration);
		userActivity.process();
	}
	
	@Test
	public void testTransitionFromSleep() {
		EventScheduler.setup(Time.GENESIS, new Time(Long.MAX_VALUE, Unit.MICROSECONDS), new EventSourceMultiplexer(new EventSource[0]));
		
		Time toSleepTimeout = new Time(5*60, Unit.SECONDS); //five minutes
		Time transitionTime = new Time(2500, Unit.MILLISECONDS); //2.5 seconds
		Time sixMinutes = new Time(6*60, Unit.SECONDS);
		Machine machine = new Machine("jurupoca", toSleepTimeout, transitionTime);
		machine.setIdle(Time.GENESIS, sixMinutes);
		
		//consumes the transition event added by the call to setIdle() and the sleep event added by the transition event
		EventScheduler.start();  
		assertEquals(2, EventScheduler.processCount());
		
		Time duration = new Time(5, Unit.SECONDS);
		UserActivity userActivity = new UserActivity(machine, sixMinutes, duration);
		userActivity.process();
		
		//consumes the transition event added by userActivity.process() and the new UserActivity event added by the 
		//transition event   
		EventScheduler.start();
		assertEquals(2, EventScheduler.processCount());
		
		List<TimeInterval> userIdlenessIntervals = machine.getUserIdlenessIntervals();
		assertEquals(1, userIdlenessIntervals.size());
		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, toSleepTimeout);
		assertTrue(userIdlenessIntervals.contains(expectedTimeInterval));
		
		List<TimeInterval> transitionIntervals = machine.getTransitionIntervals();
		assertEquals(2, transitionIntervals.size());
		expectedTimeInterval = new TimeInterval(toSleepTimeout, toSleepTimeout.plus(transitionTime));
		assertTrue(transitionIntervals.contains(expectedTimeInterval));
		expectedTimeInterval= new TimeInterval(sixMinutes, sixMinutes.plus(transitionTime));
		assertTrue(transitionIntervals.contains(expectedTimeInterval));
		
		List<TimeInterval> sleepIntervals = machine.getSleepIntervals();
		assertEquals(1, sleepIntervals.size());
		expectedTimeInterval = new TimeInterval(toSleepTimeout.plus(transitionTime), sixMinutes);
		assertTrue(sleepIntervals.contains(expectedTimeInterval));
		
		List<TimeInterval> userActivityIntervals = machine.getUserActivityIntervals();
		assertEquals(1, userActivityIntervals.size());
		expectedTimeInterval = new TimeInterval(sixMinutes.plus(transitionTime), 
				sixMinutes.plus(transitionTime).plus(duration));
		assertTrue(userActivityIntervals.contains(expectedTimeInterval));
	}
	
	//testar intervalo nao continuo
}
