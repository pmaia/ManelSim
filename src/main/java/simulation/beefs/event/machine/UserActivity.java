package simulation.beefs.event.machine;

import simulation.beefs.event.MachineDelaybleEvent;
import simulation.beefs.model.Machine;
import core.Time;

/**
 *
 * @author Patrick Maia
 */
public class UserActivity extends MachineDelaybleEvent {
	
	private final Machine host;
	private final Time duration;
	
	public UserActivity(Machine host, Time scheduledTime, Time duration) {
		this(host, scheduledTime, duration, true);
	}
	
	public UserActivity(Machine host, Time scheduledTime, Time duration, boolean delayable) {
		super(host, scheduledTime, delayable);
		
		this.host = host;
		this.duration = duration;
	}
	
	@Override
	public void process() {
		host.setActive(getScheduledTime(), duration);
	}

}
