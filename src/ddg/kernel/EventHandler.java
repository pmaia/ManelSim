package ddg.kernel;

import ddg.emulator.EventsGeneratedBySimulationQueue;


/**
 * TODO make doc
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public abstract class EventHandler {

	private final EventsGeneratedBySimulationQueue eventsGeneratedBySimulationQueue;
	
	/**
	 * @param scheduler
	 */
	public EventHandler(EventsGeneratedBySimulationQueue eventsGeneratedBySimulationQueue) {
		this.eventsGeneratedBySimulationQueue = eventsGeneratedBySimulationQueue;
	}

	public void send(Event event) {
		eventsGeneratedBySimulationQueue.addEvent(event);
	}
	
	/**
	 * @param jeevent
	 */
	public abstract void handleEvent(Event jeevent);

}
