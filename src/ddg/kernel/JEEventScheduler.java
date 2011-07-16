/* JEEventScheduler - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package ddg.kernel;

import java.util.Vector;

/**
 * TODO make doc
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public final class JEEventScheduler {

	private JETime now = new JETime(0L);
	private Vector<JEEvent> eventList = new Vector<JEEvent>();
	private Vector<JEEventHandler> handlerList;
	private Boolean isActive;
	private JETime theEmulationEnd;

	/**
     * 
     */
	public JEEventScheduler() {
		this(null);
	}

	/**
	 * @param emulationEnd
	 */
	public JEEventScheduler(JETime emulationEnd) {

		eventList.setSize(10000);
		eventList.clear();
		handlerList = new Vector<JEEventHandler>();
		handlerList.setSize(100);
		handlerList.clear();
		isActive = Boolean.valueOf(false);
		theEmulationEnd = emulationEnd;
	}

	/**
	 * @param aNewEvent
	 */
	public void schedule(JEEvent aNewEvent) {

		JETime anEventTime = aNewEvent.getTheScheduledTime();

		if (anEventTime.isEarlierThan(now())) {
			throw new RuntimeException("ERROR: emulation time(" + now()
					+ ") already ahead of event time(" + anEventTime
					+ "). Event is outdated and will not be processed.");
		}

		int queueSize = eventList.size();

		if (queueSize == 0) {
			eventList.addElement(aNewEvent);
		} else if (eventList.lastElement().getTheScheduledTime()
				.isEarlierThan(anEventTime)) {
			eventList.addElement(aNewEvent);
		} else {

			int queuePos;

			for (queuePos = queueSize - 1; ((queuePos > 0) & anEventTime
					.isEarlierThan(eventList.elementAt(queuePos)
							.getTheScheduledTime())); queuePos--) {
				/* empty */
			}

			if (++queuePos == 1
					& anEventTime.isEarlierThan(eventList.elementAt(0)
							.getTheScheduledTime())) {
				queuePos--;
			}

			eventList.insertElementAt(aNewEvent, queuePos);
		}
	}

	/**
	 * @param anObsoleteEvent
	 */
	public void cancelEvent(JEEvent anObsoleteEvent) {

		if (anObsoleteEvent == null) {
			throw new NullPointerException();
		}
		eventList.remove(anObsoleteEvent);
	}

	/**
	 * @param aNewEventHandler
	 * @return
	 */
	public JEEventScheduler registerHandler(JEEventHandler aNewEventHandler) {

		Integer aHandlerId = aNewEventHandler.getHandlerId();

		if(handlerList.size() < aHandlerId.intValue() - 1) {
			handlerList.setSize(aHandlerId.intValue() - 1);
		}

		if(handlerList.size() > aHandlerId.intValue() - 1) {
			handlerList.removeElementAt(aHandlerId.intValue() - 1);
		}

		handlerList
				.insertElementAt(aNewEventHandler, aHandlerId.intValue() - 1);
		return this;
	}

	/**
     * 
     */
	public void start() {

		if (!eventList.isEmpty()) {
			schedule();
		}
	}

	/**
	 * @return
	 */
	private JEEvent peek() {

		if (!eventList.isEmpty()) {
			JEEvent aNextEvent = eventList.elementAt(0);
			eventList.removeElementAt(0);
			return aNextEvent;
		}

		return null;
	}

	/**
     * 
     */
	private void schedule() {

		isActive = Boolean.valueOf(true);

		while (!eventList.isEmpty() & isActive.booleanValue()
				& isEarlierThanEmulationEnd(now())) {

			JEEvent aNextEvent = peek();

			if (aNextEvent != null) {

				JETime anEventTime = aNextEvent.getTheScheduledTime();

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

		isActive = Boolean.valueOf(false);
	}

	private boolean isEarlierThanEmulationEnd(JETime time) {
		return (theEmulationEnd != null) ? time.isEarlierThan(theEmulationEnd)
				: true;
	}

	/**
	 * @param aNextEvent
	 */
	private void processEvent(JEEvent aNextEvent) {

		Integer aTargetHandlerId = aNextEvent.getTheTargetHandlerId();

		if (handlerList.elementAt(aTargetHandlerId.intValue() - 1) == null) {
			throw new RuntimeException("ERROR: no Handler at vector position "
					+ (aTargetHandlerId.intValue() - 1)
					+ ". Something's wrong here, dude.");
		}

		handlerList.elementAt(aTargetHandlerId.intValue() - 1).handleEvent(
				aNextEvent);
	}

	/**
	 * @return
	 */
	public JETime now() {
		return now;
	}
}