package ddg.emulator.events;

import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.Time;

public class MachineStateTransitionEvent extends Event {
	
	public static final String EVENT_NAME = "state-transition";

	/**
	 * 
	 * @param aHandler
	 * @param aScheduledTime
	 */
	public MachineStateTransitionEvent(EventHandler aHandler,
			Time aScheduledTime) {
		super(EVENT_NAME, aHandler, aScheduledTime);
	}
}
