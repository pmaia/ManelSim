package ddg.emulator.events;

import ddg.kernel.JEEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JETime;

public class CancelPendingMachineStateTransitionsEvent extends JEEvent {
	
	public static final String EVENT_NAME = "cancel-pending-machine-state-transition";

	public CancelPendingMachineStateTransitionsEvent(
			JEEventHandler aHandler, JETime aScheduledTime) {
		super(EVENT_NAME, aHandler, aScheduledTime);
	}

}
