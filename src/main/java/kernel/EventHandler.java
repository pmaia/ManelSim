package kernel;

import java.util.PriorityQueue;


/**
 * TODO make doc
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public abstract class EventHandler {

	private final PriorityQueue<Event> eventsGeneratedBySimulationQueue;
	
	/**
	 * @param scheduler
	 */
	public EventHandler(PriorityQueue<Event> eventsGeneratedBySimulationQueue) {
		this.eventsGeneratedBySimulationQueue = eventsGeneratedBySimulationQueue;
	}

	public void send(Event event) {
		eventsGeneratedBySimulationQueue.add(event);
	}
	
	/**
	 * @param jeevent
	 */
	public abstract void handleEvent(Event jeevent);

}
