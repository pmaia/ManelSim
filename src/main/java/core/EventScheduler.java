package core;

import core.Time.Unit;

/**
 * TODO make doc
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public final class EventScheduler {

	private Time now = new Time(0L, Unit.MILLISECONDS);
	private Time theEmulationEnd;
	private EventSource eventSource;

	public EventScheduler(EventSource eventSource) {
		this(null, eventSource);
	}

	/*
	 * FIXME I think it would be good to have emulation start and end times
	 */
	public EventScheduler(Time emulationEnd, EventSource eventSource) {
		theEmulationEnd = emulationEnd;
		this.eventSource = eventSource;
	}

	public void start() {

		Event nextEvent;
		
		while ((nextEvent = eventSource.getNextEvent()) != null && isEarlierThanEmulationEnd(now())) {
			Time eventTime = nextEvent.getScheduledTime();

			if (eventTime.isEarlierThan(now())) {
				throw new RuntimeException("ERROR: emulation time(" + now()
						+ ") " + "already ahead of event time("
						+ eventTime
						+ "). Event is outdated and will not be processed.");
			}

			if (isEarlierThanEmulationEnd(eventTime)) {
				now = eventTime;
				processEvent(nextEvent);
			} else {
				now = theEmulationEnd;
			}
		}

	}

	/* FIXME do I really need this method? If, by default I set theEmulationEnd = Time.THE_END_OF_THE_WORLD everything 
	 * would work without this method, right?  
	 */
	private boolean isEarlierThanEmulationEnd(Time time) {
		return (theEmulationEnd != null) ? time.isEarlierThan(theEmulationEnd) : true;
	}

	private void processEvent(Event nextEvent) {
		nextEvent.process();
	}

	public Time now() {
		return now;
	}

}