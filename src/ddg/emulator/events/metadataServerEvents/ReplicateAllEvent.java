package ddg.emulator.events.metadataServerEvents;

import ddg.kernel.JEEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JETime;

public class ReplicateAllEvent extends JEEvent {
	
	public static final String EVENT_NAME = "replicate_all";

	public ReplicateAllEvent(JEEventHandler aHandler, JETime aScheduledTime) {
		super(EVENT_NAME, aHandler, aScheduledTime);
	}

}
