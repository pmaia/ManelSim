package simulation.beefs.event.filesystem;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.ReplicatedFile;
import core.Event;
import core.Time;

/**
 * 
 * @author Patrick Maia
 *
 */
public class Read extends Event {

	private final long bytesTransfered;
	private final String filePath;
	private final FileSystemClient client;
	private final Time duration;

	public Read(FileSystemClient client, Time scheduledTime, Time duration, String filePath, long bytesTransfered) {
		super(scheduledTime);
		
		this.duration = duration;
		this.client = client;
		this.filePath = filePath;
		this.bytesTransfered = bytesTransfered;
	}

	@Override
	public String toString() {
		return "read\t" + getScheduledTime() + "\t" + filePath + "\t" + bytesTransfered;
	}

	@Override
	public void process() {
		ReplicatedFile file = client.createOrOpen(filePath);
		
		DataServer primary = file.getPrimary();
		primary.reportRead(getScheduledTime(), duration);
	}

}