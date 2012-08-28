package simulation.beefs.util;

import core.Event;
import core.Time;

/**
 * 
 * Handy {@link Event} to advance EventScheduler time to a desired point.
 * 
 * @author Patrick Maia
 *
 */
public class Noop extends Event {

	public Noop(Time scheduledTime) {
		super(scheduledTime);
	}

	@Override
	public void process() {
		// nothing
	}

}
