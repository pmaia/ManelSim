package simulation.beefs.event.machine;

import simulation.beefs.model.Machine;
import core.Event;
import core.Time;

/**
 * @author Patrick Maia
 */
public class WakeOnLan extends Event {
	
	private final Machine machine;

	public WakeOnLan(Machine machine, Time scheduledTime) {
		super(scheduledTime);
		this.machine = machine;
	}

	@Override
	public void process() {
		machine.wakeOnLan();
	}

}
