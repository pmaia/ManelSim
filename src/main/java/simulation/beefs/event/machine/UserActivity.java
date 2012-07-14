package simulation.beefs.event.machine;

import simulation.beefs.model.Machine;
import core.Event;
import core.Time;

/**
 *
 * @author Patrick Maia
 */
public class UserActivity extends Event {
	
	private final Machine host;
	private final Time duration;
	
	public UserActivity(Machine host, Time scheduledTime, Time duration) {
		super(scheduledTime);
		
		this.host = host;
		this.duration = duration;
	}
	
	@Override
	public void process() {
		host.setActive(getScheduledTime(), duration);
	}

}
