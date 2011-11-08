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

	private Integer theTargetHandlerId;
	private Time scheduledTime;

	/**
	 * @param aName
	 * @param aHandler
	 * @param aScheduledTime
	 */
	public Event(String aName, EventHandler aHandler, Time aScheduledTime) {

		myEventId = eventId++;

		name = aName;
		theTargetHandlerId = aHandler.getHandlerId();
		scheduledTime = aScheduledTime;
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

	/**
	 * @return
	 */
	public int getEventId() {
		return myEventId;
	}

	/**
	 * @param theTargetHandlerId
	 */
	public void setTheTargetHandlerId(Integer theTargetHandlerId) {
		this.theTargetHandlerId = theTargetHandlerId;
	}
	
	@Override
	public int compareTo(Event o) {
		return this.scheduledTime.compareTo(o.scheduledTime);
	}
}