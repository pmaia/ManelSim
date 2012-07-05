package simulation.beefs.event.filesystem;

import simulation.beefs.model.ReplicatedFile;
import core.Event;
import core.Time;

public class DeleteFileReplicas extends Event {

	public DeleteFileReplicas(Time scheduledTime, ReplicatedFile file) {
		super(scheduledTime);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub

	}

}
