package simulation.beefs.event;

import simulation.beefs.model.Machine;
import core.Event;
import core.Time;

/**
 * @author Patrick Maia - patrick@lsd.ufcg.edu.br
 */
public abstract class MachineDelaybleEvent extends Event {

	private final Machine machine;
	
	private final boolean delayable;
	
	public MachineDelaybleEvent(Machine machine, Time scheduledTime, boolean delayable) {
		super(scheduledTime);
		
		this.machine = machine;
		this.delayable = delayable;
	}
	
	@Override
	public Time getScheduledTime() {
		Time delay = delayable ? machine.currentDelay() : Time.GENESIS;
		return super.getScheduledTime().plus(delay);
	}

	@Override
	public abstract void process();

}
