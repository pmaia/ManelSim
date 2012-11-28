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
	
	public static class MachineStateInterval {
		private final State state;
		private final TimeInterval interval;
		public MachineStateInterval(State state, TimeInterval interval) {
			this.state = state;
			this.interval = interval;
		}
		public State getState() {
			return state;
		}
		public TimeInterval getInterval() {
			return interval;
		}
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
	
	private final List<MachineStateInterval> stateIntervals = new ArrayList<MachineStateInterval>();
	
	public Machine(String hostname, Time toSleepTimeout, Time transitionDuration) {
		this.hostname = hostname;
		this.toSleepTimeout = toSleepTimeout;
		this.transitionDuration = transitionDuration;
//		this.currentState = new Bootstrap();
		//FIXME: My simulations are not related to this machine state abstraction, and I had problems
		//do understand how to change it. So i add this hack to be active from the beginning. I know
		//it's ugly.
		this.currentState = new Active(new TimeInterval(Time.GENESIS, Time.THE_FINAL_JUDGMENT));
	}
	
	public String getName() {
		return hostname;
	}

	public List<TimeInterval> getUserActivityIntervals() {
		State [] states = {State.ACTIVE};
		return getIntervals(states);
	}
	
	public List<TimeInterval> getUserIdlenessIntervals() {
		State [] states = {State.IDLE};
		return getIntervals(states);
	}

	public List<TimeInterval> getTransitionIntervals() {
		State [] states = {State.GOING_SLEEP, State.WAKING_UP};
		return getIntervals(states);
	}

	public List<TimeInterval> getSleepIntervals() {
		State [] states = {State.SLEEPING};
		return getIntervals(states);
	}
	
	public List<MachineStateInterval> getStateIntervals() {
		return new ArrayList<MachineStateInterval>(stateIntervals);
	}
	
	private List<TimeInterval> getIntervals(State [] states) {
		List<TimeInterval> intervals = new ArrayList<TimeInterval>();
		for(MachineStateInterval stateInterval : stateIntervals) {
			for(State state : states) {
				if(stateInterval.getState() == state) {
					intervals.add(stateInterval.getInterval());
					break;
				}
			}
		}
		return intervals;
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
	
	private void checkContinuity(TimeInterval next) {
		TimeInterval last = stateIntervals.get(stateIntervals.size() - 1).getInterval();
		if(!last.isContiguous(next)) {
			String msg = String.format("The interval duration of the next state must be contiguous to the " +
					"interval duration of the current state. Current interval is %s. You tried this %s.", last, next);
			throw new IllegalArgumentException(msg);
		}
	}
	
	// the next four methods are used by State implementations to schedule new events
	private void scheduleSleep(Time begin, Time duration) {
		EventScheduler.schedule(new Sleep(this, begin, duration));
	}
	
	private void scheduleUserActivity(Time begin, Time duration) {
		EventScheduler.schedule(new UserActivity(this, begin, duration, false));
	}
	
	private void scheduleUserIdleness(Time begin, Time duration) {
		EventScheduler.schedule(new UserIdleness(this, begin, duration, false));
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
			stateIntervals.add(new MachineStateInterval(State.IDLE, interval));
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			if(sleepIsExpected) {
				throw new IllegalStateException("transition to SLEEP is expected.");
			}
			checkContinuity(interval); 
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
			checkContinuity(interval);
			
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
			stateIntervals.add(new MachineStateInterval(State.ACTIVE, interval));
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			throw new IllegalStateException("This machine is already ACTIVE.");
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			checkContinuity(interval);
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
			stateIntervals.add(new MachineStateInterval(State.SLEEPING, interval));
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			checkContinuity(interval);
			
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
			int lastElementIndex = stateIntervals.size() - 1;
			TimeInterval shouldSleepInterval = stateIntervals.get(lastElementIndex).getInterval();
			if(shouldSleepInterval.end().isEarlierThan(when)) {
				throw new IllegalStateException("This machine should already be awake.");
			}
			stateIntervals.remove(lastElementIndex);
			TimeInterval interval = new TimeInterval(shouldSleepInterval.begin(), when);
			stateIntervals.add(new MachineStateInterval(State.SLEEPING, interval));

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
			stateIntervals.add(new MachineStateInterval(State.GOING_SLEEP, interval));
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
			checkContinuity(interval);
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
		
		private boolean neverEnteredHereBefore = true;
				
		public WakingUp(Time time, boolean expectTransitionToIdle) {
			transitionInterval = new TimeInterval(time, time.plus(transitionDuration));
			stateIntervals.add(new MachineStateInterval(State.WAKING_UP, transitionInterval));
			currentDelay = currentDelay.plus(transitionDuration);
			this.expectTransitionToIdle = expectTransitionToIdle;
		}
		@Override
		public MachineState toActive(TimeInterval interval) {
			if(expectTransitionToIdle) {
				throw new IllegalStateException("Transition to IDLE is expected.");
			}
			
			if(interval.begin().compareTo(transitionInterval.begin()) >= 0 &&
					interval.begin().compareTo(transitionInterval.end()) < 0) { //FIXME add a comment explaining when does it happen
			
				if(!neverEnteredHereBefore) {
					throw new IllegalStateException("Right call, wrong time." +
							" This was expected by the end of the current transition.");
				}
				scheduleUserActivity(transitionInterval.end(), interval.delta());
				// adjusts the machine delay caused by this transition
				currentDelay = 
					currentDelay.minus(transitionDuration).plus(transitionInterval.end().minus(interval.begin()));
				neverEnteredHereBefore = false;
				return this;
			} else {
				checkContinuity(interval);
				return new Active(interval);
			}
		}
		@Override
		public MachineState toIdle(TimeInterval interval) {
			if(!expectTransitionToIdle) {
				throw new IllegalStateException("Transition to ACTIVE is expected.");
			}
			checkContinuity(interval);
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
