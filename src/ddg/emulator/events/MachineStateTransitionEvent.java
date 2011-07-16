package ddg.emulator.events;

import ddg.kernel.JEEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JETime;

public class MachineStateTransitionEvent extends JEEvent {
	
	public static final String EVENT_NAME = "state-transition";

	/**
	 * 
	 * @param aHandler
	 * @param aScheduledTime
	 */
	public MachineStateTransitionEvent(JEEventHandler aHandler,
			JETime aScheduledTime) {
		super(EVENT_NAME, aHandler, aScheduledTime);
	}
}
