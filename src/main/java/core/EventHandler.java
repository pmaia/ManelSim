package core;

import java.util.PriorityQueue;


/**
 * TODO make doc
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public abstract class EventHandler {

	private final PriorityQueue<Event> eventsGeneratedBySimulationQueue;
	
	public EventHandler(PriorityQueue<Event> eventsGeneratedBySimulationQueue) {
		this.eventsGeneratedBySimulationQueue = eventsGeneratedBySimulationQueue;
	}

	public void send(Event event) {
		eventsGeneratedBySimulationQueue.add(event);
	}
	
	public abstract void handleEvent(Event jeevent);

}
