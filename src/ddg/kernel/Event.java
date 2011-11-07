/* JEEvent - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package ddg.kernel;


/**
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public abstract class Event {

	private static int eventId = 0;

	private final int myEventId;
	private final String name;

	private Integer theTargetHandlerId;
	private Time theScheduledTime;

	/**
	 * @param aName
	 * @param aHandler
	 * @param aScheduledTime
	 */
	public Event(String aName, EventHandler aHandler, Time aScheduledTime) {

		myEventId = eventId++;

		name = aName;
		theTargetHandlerId = aHandler.getHandlerId();
		theScheduledTime = aScheduledTime;
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
		return theScheduledTime;
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
}