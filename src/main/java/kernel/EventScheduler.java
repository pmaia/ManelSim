package kernel;

import kernel.Time.Unit;
import emulator.EventSource;

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

	/**
     * 
     */
	public EventScheduler(EventSource eventSource) {
		this(null, eventSource);
	}

	/**
	 * @param emulationEnd
	 */
	public EventScheduler(Time emulationEnd, EventSource eventSource) {
		theEmulationEnd = emulationEnd;
		this.eventSource = eventSource;
	}

	/**
     * 
     */
	public void start() {

		Event aNextEvent;
		
		while ((aNextEvent = eventSource.getNextEvent()) != null && isEarlierThanEmulationEnd(now())) {
			Time anEventTime = aNextEvent.getScheduledTime();

			if (anEventTime.isEarlierThan(now())) {
				throw new RuntimeException("ERROR: emulation time(" + now()
						+ ") " + "already ahead of event time("
						+ anEventTime
						+ "). Event is outdated and will not be processed.");
			}

			if (isEarlierThanEmulationEnd(anEventTime)) {
				now = anEventTime;
				processEvent(aNextEvent);
			} else {
				now = theEmulationEnd;
			}
		}

	}

	private boolean isEarlierThanEmulationEnd(Time time) {
		return (theEmulationEnd != null) ? time.isEarlierThan(theEmulationEnd) : true;
	}

	/**
	 * @param aNextEvent
	 */
	private void processEvent(Event aNextEvent) {
		aNextEvent.getHandler().handleEvent(aNextEvent); //
	}

	/**
	 * @return
	 */
	public Time now() {
		return now;
	}

}