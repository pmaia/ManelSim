package simulation.beefs.model;

import java.util.ArrayList;
import java.util.List;

import simulation.beefs.event.machine.Sleep;
import simulation.beefs.event.machine.UserActivity;
import simulation.beefs.event.machine.UserIdleness;
import simulation.beefs.event.machine.WakeOnLan;
import core.EventScheduler;
import core.Time;
import core.TimeInterval;

/**
 *
 * @author Patrick Maia
 */
public class Machine {
	
	public enum State {
		IDLE,
		ACTIVE,
		SLEEPING,
		GOING_SLEEP,
		WAKING_UP,
		BOOTSTRAP
	}
	
	private interface MachineState {
		MachineState toActive(TimeInterval interval);
		MachineState toIdle(TimeInterval interval);
		MachineState toSleep(TimeInterval interval);
		MachineState wakeOnLan(Time when);
		State state();
	}

	private MachineState currentState;
	
	private Time currentDelay = Time.GENESIS;
	
	private final String hostname;
	
	private final Time toSleepTimeout;

	private final Time transitionDuration;
	
	private final List<TimeInterval> userActivityIntervals = new ArrayList<TimeInterval>();
	
	private final List<TimeInterval> userIdlenessIntervals = new ArrayList<TimeInterval>();
	
	private final List<TimeInterval> transitionIntervals = new ArrayList<TimeInterval>();
	
	private final List<TimeInterval> sleepIntervals = new ArrayList<TimeInterval>();
	
	public Machine(String hostname, Time toSleepTimeout, Time transitionDuration) {
		this.hostname = hostname;
		this.toSleepTimeout = toSleepTimeout;
		this.transitionDuration = transitionDuration;
		this.currentState = new Bootstrap();
	}
	
	public String getName() {
		return hostname;
	}

	public List<TimeInterval> getUserActivityIntervals() {
		return new ArrayList<TimeInterval>(userActivityIntervals);
	}
	
	public List<TimeInterval> getUserIdlenessIntervals() {
		return new ArrayList<TimeInterval>(userIdlenessIntervals);
	}

	public List<TimeInterval> getTransitionIntervals() {
		return new ArrayList<TimeInterval>(transitionIntervals);
	}

	public List<TimeInterval> getSleepIntervals() {
		return new ArrayList<TimeInterval>(sleepIntervals);
	}
	
	public boolean isReachable() {
		return (currentState.state() == State.ACTIVE || currentState.state() == State.IDLE);
	}

	public Time getTransitionDuration() {
		return transitionDuration;
	}
	
	public Time currentDelay() {
		return currentDelay;
	}

	public void setActive(Time begin, Time duration) {
		TimeInterval interval = new TimeInterval(begin, begin.plus(duration));
		currentState = currentState.toActive(interval);
	}
	
	public void setIdle(Time begin, Time duration) {
		TimeInterval interval = new TimeInterval(begin, begin.plus(duration));
		currentState = currentState.toIdle(interval);
	}
	
	public void setSleeping(Time begin, Time duration) {
		TimeInterval interval = new TimeInterval(begin, begin.plus(duration));
		currentState = currentState.toSleep(interval);
	}
	
	public void wakeOnLan(Time when) {
		currentState = currentState.wakeOnLan(when);
	}
	
	public State getState() {
		return currentState.state();
	}
	
	private void checkContinuity(List<TimeInterval> currentStateIntervals, TimeInterval next) {
		TimeInterval last = currentStateIntervals.get(currentStateIntervals.size() - 1);
		if(!last.isContiguous(next)) {
			throw new IllegalArgumentException("The interval duration of the next state must be contiguous to the " +
					"interval duration of the current state.");
		}
	}
	
	// the next four methods are used by State implementations to schedule new events
	private void scheduleSleep(Time begin, Time duration) {
		EventScheduler.schedule(new Sleep(this, begin, duration));
	}
	
	private void scheduleUserActivity(Time begin, Time duration) {
		EventScheduler.schedule(new UserActivity(this, begin, duration));
	}
	
	private void scheduleUserIdleness(Time begin, Time duration) {
		EventScheduler.schedule(new UserIdleness(this, begin, duration));
	}
	
	private void scheduleWakeOnLan(Time when) {
		EventScheduler.schedule(new WakeOnLan(this, when));
	}
	//
	
	private class Bootstrap implements MachineState {
		@Override
		public MachineState toActive(TimeInterval interval) {
			throw new IllegalStateException("transition to IDLE is expected.");
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			return new Idle(interval);
		}
		@Override
		public MachineState toSleep(TimeInterval interval) {
			throw new IllegalStateException("transition to IDLE is expected.");
		}
		@Override
		public MachineState wakeOnLan(Time when) {
			throw new IllegalStateException("transition to IDLE is expected.");
		}
		@Override
		public State state() {
			return State.BOOTSTRAP;
		}
	}
	
	private class Idle implements MachineState {
		
		private boolean sleepIsExpected = false;
		
		public Idle(TimeInterval interval) {
			if(toSleepTimeout.isEarlierThan(interval.delta())) { // then, schedule a sleep event on now + toSleepTimeout
				Time sleepBegin = interval.begin().plus(toSleepTimeout);
				scheduleSleep(sleepBegin, interval.end().minus(sleepBegin));
				sleepIsExpected = true;
				interval = new TimeInterval(interval.begin(), interval.begin().plus(toSleepTimeout));
			}
			userIdlenessIntervals.add(interval);
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			if(sleepIsExpected) {
				throw new IllegalStateException("transition to SLEEP is expected.");
			}
			checkContinuity(userIdlenessIntervals, interval); 
			return new Active(interval);
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			throw new IllegalStateException("This machine is already IDLE.");
		}
		@Override
		public MachineState toSleep(TimeInterval interval) {
			if(!sleepIsExpected) {
				throw new IllegalStateException("transition to ACTIVE is expected");
			}
			checkContinuity(userIdlenessIntervals, interval);
			
			Time sleepDuration =  Time.max(interval.delta().minus(transitionDuration), Time.GENESIS);
			scheduleSleep(interval.begin().plus(transitionDuration), sleepDuration);
			
			return new GoingSleep(interval.begin());
		}
		@Override
		public MachineState wakeOnLan(Time when) {
			throw new IllegalStateException("This machine is not sleeping.");
		}
		@Override
		public State state() {
			return State.IDLE;
		}
	}
	
	private class Active implements MachineState {
		public Active(TimeInterval interval) {
			userActivityIntervals.add(interval);
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			throw new IllegalStateException("This machine is already ACTIVE.");
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			checkContinuity(userActivityIntervals, interval);
			return new Idle(interval);
		}
		@Override
		public MachineState toSleep(TimeInterval interval) {
			throw new IllegalStateException("Transition to IDLE is expected.");
		}
		@Override
		public MachineState wakeOnLan(Time when) {
			throw new IllegalStateException("This machine is not sleeping.");
		}
		@Override
		public State state() {
			return State.ACTIVE;
		}
	}
	
	private class Sleeping implements MachineState {
		public Sleeping(TimeInterval interval) {
			sleepIntervals.add(interval);
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			checkContinuity(sleepIntervals, interval);
			
			scheduleUserActivity(interval.begin().plus(transitionDuration), interval.delta());
			
			return new WakingUp(interval.begin(), false);
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			throw new IllegalStateException("Transition to ACTIVE or WakeOnLan are expected.");
		}
		@Override
		public MachineState toSleep(TimeInterval interval) {
			throw new IllegalStateException("Transition to ACTIVE or WakeOnLan are expected.");
		}
		@Override
		public MachineState wakeOnLan(Time when) {
			/*
			 *  adjusts the time interval the machine really slept
			 */
			int lastElementIndex = sleepIntervals.size() - 1;
			TimeInterval shouldSleepInterval = sleepIntervals.get(lastElementIndex);
			if(shouldSleepInterval.end().isEarlierThan(when)) {
				throw new IllegalStateException("This machine should already be awake.");
			}
			sleepIntervals.remove(lastElementIndex);
			sleepIntervals.add(new TimeInterval(shouldSleepInterval.begin(), when));

			Time idlenessDuration = Time.max(shouldSleepInterval.end().minus(when).minus(transitionDuration), 
					Time.GENESIS);
			
			if(idlenessDuration.equals(Time.GENESIS)) {
				return new WakingUp(when, false);
			} else {
				/* 
				 * schedules a new UserIdleness event starting after the transition ends and lasting the same time this 
				 * machine should remain sleeping (before being disturbed) minus the transition duration.
				 */
				scheduleUserIdleness(when.plus(transitionDuration), idlenessDuration);
				return new WakingUp(when, true);
			}
		}
		@Override
		public State state() {
			return State.SLEEPING;
		}
	}
	
	private class GoingSleep implements MachineState {
		
		private final Time transitionEnd;
		private boolean transitionToActiveMayOccur = true;
		private boolean wakeOnLanScheduled = false;
		
		public GoingSleep(Time time) {
			TimeInterval interval = new TimeInterval(time, time.plus(transitionDuration));
			transitionIntervals.add(interval);
			transitionEnd = interval.end();
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			if(!transitionToActiveMayOccur) {
				throw new IllegalStateException("Transition to ACTIVE already occured.");
			}
			if(!interval.begin().isEarlierThan(transitionEnd) || 
					interval.begin().isEarlierThan(transitionEnd.minus(transitionDuration))) {
				throw new IllegalArgumentException("I could accept this transition at another time.");
			}
			currentDelay = currentDelay.plus(transitionEnd.minus(interval.begin()));
			scheduleUserActivity(transitionEnd, interval.delta());
			transitionToActiveMayOccur = false;

			return this;
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			throw new IllegalStateException("Transition to ACTIVE, WakeOnLan or SLEEPING are expected.");
		}
		@Override
		public MachineState toSleep(TimeInterval interval) {
			checkContinuity(transitionIntervals, interval);
			return new Sleeping(interval);
		}
		@Override
		public MachineState wakeOnLan(Time when) {
			if(!wakeOnLanScheduled) {
				scheduleWakeOnLan(transitionEnd);
				wakeOnLanScheduled = true;
			}
			return this;
		}
		@Override
		public State state() {
			return State.GOING_SLEEP;
		}
	}
	
	private class WakingUp implements MachineState {
		
		private final TimeInterval transitionInterval;
		private final boolean expectTransitionToIdle;
		
		private Time delay = transitionDuration;
		private boolean neverEnteredHereBefore = true;
				
		public WakingUp(Time time, boolean expectTransitionToIdle) {
			transitionInterval = new TimeInterval(time, time.plus(transitionDuration));
			transitionIntervals.add(transitionInterval);
			
			this.expectTransitionToIdle = expectTransitionToIdle;
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			if(expectTransitionToIdle) {
				throw new IllegalStateException("Transition to IDLE is expected.");
			}
			
			if(interval.begin().compareTo(transitionInterval.begin()) >= 0 &&
					interval.begin().compareTo(transitionInterval.end()) < 0) { 
			
				if(!neverEnteredHereBefore) {
					throw new IllegalStateException("Right call, wrong time." +
							" This was expected by the end of the current transition.");
				}
				scheduleUserActivity(transitionInterval.end(), interval.delta());
				delay = transitionInterval.end().minus(interval.begin());
				neverEnteredHereBefore = false;
				return this;
			} else {
				checkContinuity(transitionIntervals, interval);
				currentDelay = currentDelay.plus(delay);
				return new Active(interval);
			}
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			if(!expectTransitionToIdle) {
				throw new IllegalStateException("Transition to ACTIVE is expected.");
			}
			checkContinuity(transitionIntervals, interval);
			return new Idle(interval);
		}
		@Override
		public MachineState toSleep(TimeInterval interval) {
			String nextState = expectTransitionToIdle ? "IDLE" : "ACTIVE";
			throw new IllegalStateException(String.format("Transition to %s is expected.", nextState));
		}
		@Override
		public MachineState wakeOnLan(Time when) {
			return this;
		}
		@Override
		public State state() {
			return State.WAKING_UP;
		}
	}
	
}
