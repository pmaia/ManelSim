/* JEEvent - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package ddg.kernel;


/**
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public abstract class Event implements Comparable<Event> {

	private final String name;

	private final EventHandler handler;
	private final Time scheduledTime;
	private final Time duration;

	public Event(String name, EventHandler handler, Time scheduledTime, Time duration) {

		this.name = name;
		this.handler = handler;
		this.scheduledTime = scheduledTime;
		this.duration = duration;
	}

	/**
	 * @return
	 */
	public EventHandler getHandler() {
		return handler;
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

	@Override
	public int compareTo(Event o) {
		return this.scheduledTime.compareTo(o.scheduledTime);
	}
}