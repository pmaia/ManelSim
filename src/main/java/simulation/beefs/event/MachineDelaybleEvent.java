package simulation.beefs.event;

import simulation.beefs.model.Machine;
import core.Event;
import core.Time;

/**
 * @author Patrick Maia - patrick@lsd.ufcg.edu.br
 */
public abstract class MachineDelaybleEvent extends Event {

	private final Machine machine;
	
	public MachineDelaybleEvent(Machine machine, Time scheduledTime) {
		super(scheduledTime);
		
		this.machine = machine;
	}
	
	@Override
	public Time getScheduledTime() {
		return super.getScheduledTime().plus(machine.currentDelay());
	}

	@Override
	public abstract void process();

}
