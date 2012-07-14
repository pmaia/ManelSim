package simulation.beefs.event.machine;

import simulation.beefs.model.Machine;
import core.Event;
import core.Time;

/**
 *
 * @author Patrick Maia
 */
public class Sleep extends Event {
	
	private final Machine machine; 
	
	private final Time duration;
	
	public Sleep(Machine machine, Time scheduledTime, Time duration) {
		super(scheduledTime);
		
		this.machine = machine;
		this.duration = duration;
	}
	
	@Override
	public void process() {
		machine.setSleeping(getScheduledTime(), duration);
	}
	
}
