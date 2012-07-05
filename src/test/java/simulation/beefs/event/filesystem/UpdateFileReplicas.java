package simulation.beefs.event.filesystem;

import core.Event;
import core.Time;

public class UpdateFileReplicas extends Event {
	
	private String filePath;

	public UpdateFileReplicas(Time scheduledTime, String filePath) {
		super(scheduledTime);
		
		this.filePath = filePath;
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpdateFileReplicas other = (UpdateFileReplicas) obj;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		return true;
	}

}
