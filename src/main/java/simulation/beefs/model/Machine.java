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
	
	public enum Status {
		IDLE,
		ACTIVE,
		SLEEPING,
		TRANSITION,
		BOOTSTRAP
	}
	
	private interface State {
		State toActive(TimeInterval interval);
		State toIdle(TimeInterval interval);
		State toSleep(TimeInterval interval);
		State wakeOnLan();
		Status status();
	}

	private State currentState;
	
	private final String hostname;
	
	private final Time toSleepTimeout;

	private final Time transitionDuration;
	
	private final List<TimeInterval> userActivityIntervals = new ArrayList<TimeInterval>();
	
	private final List<TimeInterval> userIdlenessIntervals = new ArrayList<TimeInterval>();
	
	private final List<TimeInterval> transitionIntervals = new ArrayList<TimeInterval>();
	
	private final List<TimeInterval> sleepIntervals = new ArrayList<TimeInterval>();
	
	public Machine(String hostname, Time toSleepTimeout, Time transitionTime) {
		this.hostname = hostname;
		this.toSleepTimeout = toSleepTimeout;
		this.transitionDuration = transitionTime;
		this.currentState = new Bootstrap();
	}
	
	public String getHostname() {
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
	
	public void wakeOnLan() {
		currentState = currentState.wakeOnLan();
	}
	
	public Status getStatus() {
		return currentState.status();
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
	
	private class Bootstrap implements State {
		@Override
		public State toActive(TimeInterval interval) {
			throw new IllegalStateException("transition to IDLE is expected.");
		}
		@Override
		public State toIdle(TimeInterval interval) {
			return new Idle(interval);
		}
		@Override
		public State toSleep(TimeInterval interval) {
			throw new IllegalStateException("transition to IDLE is expected.");
		}
		@Override
		public State wakeOnLan() {
			throw new IllegalStateException("transition to IDLE is expected.");
		}
		@Override
		public Status status() {
			return Status.BOOTSTRAP;
		}
	}
	
	private class Idle implements State {
		
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
		public State toActive(TimeInterval interval) {
			if(sleepIsExpected) {
				throw new IllegalStateException("transition to SLEEP is expected.");
			}
			checkContinuity(userIdlenessIntervals, interval); 
			return new Active(interval);
		}
		@Override
		public State toIdle(TimeInterval interval) {
			throw new IllegalStateException("This machine is already IDLE.");
		}
		@Override
		public State toSleep(TimeInterval interval) {
			if(!sleepIsExpected) {
				throw new IllegalStateException("transition to ACTIVE is expected");
			}
			checkContinuity(userIdlenessIntervals, interval);
			
			Time sleepDuration =  Time.max(interval.delta().minus(transitionDuration), Time.GENESIS);
			scheduleSleep(interval.begin().plus(transitionDuration), sleepDuration);
			
			return new Transitioning(interval.begin());
		}
		@Override
		public State wakeOnLan() {
			throw new IllegalStateException("This machine is not sleeping.");
		}
		@Override
		public Status status() {
			return Status.IDLE;
		}
	}
	
	private class Active implements State {
		public Active(TimeInterval interval) {
			userActivityIntervals.add(interval);
		}
		@Override
		public State toActive(TimeInterval interval) {
			throw new IllegalStateException("This machine is already ACTIVE.");
		}
		@Override
		public State toIdle(TimeInterval interval) {
			checkContinuity(userActivityIntervals, interval);
			return new Idle(interval);
		}
		@Override
		public State toSleep(TimeInterval interval) {
			throw new IllegalStateException("Transition to IDLE is expected.");
		}
		@Override
		public State wakeOnLan() {
			throw new IllegalStateException("This machine is not sleeping.");
		}
		@Override
		public Status status() {
			return Status.ACTIVE;
		}
	}
	
	private class Sleeping implements State {
		public Sleeping(TimeInterval interval) {
			sleepIntervals.add(interval);
		}
		@Override
		public State toActive(TimeInterval interval) {
			checkContinuity(sleepIntervals, interval);
			
			scheduleUserActivity(interval.begin().plus(transitionDuration), interval.delta());
			
			return new Transitioning(interval.begin());
		}
		@Override
		public State toIdle(TimeInterval interval) {
			throw new IllegalStateException("Transition to ACTIVE or WakeOnLan are expected.");
		}
		@Override
		public State toSleep(TimeInterval interval) {
			throw new IllegalStateException("Transition to ACTIVE or WakeOnLan are expected.");
		}
		@Override
		public State wakeOnLan() {
			/*
			 *  adjusts the time interval the machine really slept
			 */
			Time now = EventScheduler.now();
			int lastElementIndex = sleepIntervals.size() - 1;
			TimeInterval shouldSleepInterval = sleepIntervals.get(lastElementIndex);
			if(shouldSleepInterval.end().isEarlierThan(now)) {
				throw new IllegalStateException("This machine should already be awake.");
			}
			sleepIntervals.remove(lastElementIndex);
			sleepIntervals.add(new TimeInterval(shouldSleepInterval.begin(), now));
			/* 
			 * schedules a new UserIdleness event starting after the transition ends and lasting the same time this 
			 * machine should remain sleeping (before being disturbed) minus the transition duration (or zero if negative)
			 */
			Time idlenessDuration = Time.max(shouldSleepInterval.end().minus(now).minus(transitionDuration), 
					Time.GENESIS);
			scheduleUserIdleness(now.plus(transitionDuration), idlenessDuration);
			/*
			 * returns a Transitioning state
			 */
			return new Transitioning(now);
		}
		@Override
		public Status status() {
			return Status.SLEEPING;
		}
	}
	
	private class Transitioning implements State {
		
		private final Time transitionEnd;
		
		public Transitioning(Time time) {
			TimeInterval interval = new TimeInterval(time, time.plus(transitionDuration));
			transitionIntervals.add(interval);
			transitionEnd = interval.end();
		}
		@Override
		public State toActive(TimeInterval interval) {
			checkContinuity(transitionIntervals, interval);
			return new Active(interval);
		}
		@Override
		public State toIdle(TimeInterval interval) {
			checkContinuity(transitionIntervals, interval);
			return new Idle(interval);
		}
		@Override
		public State toSleep(TimeInterval interval) {
			checkContinuity(transitionIntervals, interval);
			return new Sleeping(interval);
		}
		@Override
		public State wakeOnLan() {
			scheduleWakeOnLan(transitionEnd);
			return this;
		}
		@Override
		public Status status() {
			return Status.TRANSITION;
		}
	}
	
}
