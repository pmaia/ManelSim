/* JEEvent - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package ddg.kernel;


/**
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public abstract class Event implements Comparable<Event> {

	private static int eventId = 0;

	private final int myEventId;
	private final String name;

	private final Integer theTargetHandlerId;
	private final Time scheduledTime;
	private final Time duration;

	/**
	 * @param aName
	 * @param aHandler
	 * @param scheduledTime
	 */
	public Event(String aName, EventHandler aHandler, Time scheduledTime, Time duration) {

		myEventId = eventId++;

		name = aName;
		theTargetHandlerId = aHandler.getHandlerId();
		this.scheduledTime = scheduledTime;
		this.duration = duration;
	}

	/**
	 * @return
	 */
	public Integer getTheTargetHandlerId() {
		return theTargetHandlerId;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public Time getScheduledTime() {
		return scheduledTime;
	}
	
	public Time getDuration() {
		return this.duration;
	}

	/**
	 * @return
	 */
	public int getEventId() {
		return myEventId;
	}

	@Override
	public int compareTo(Event o) {
		return this.scheduledTime.compareTo(o.scheduledTime);
	}
}