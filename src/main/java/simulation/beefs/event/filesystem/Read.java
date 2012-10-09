package simulation.beefs.event.filesystem;

import simulation.beefs.event.MachineDelaybleEvent;
import simulation.beefs.model.FileSystemClient;
import core.Time;

/**
 * 
 * @author Patrick Maia
 *
 */
public class Read extends MachineDelaybleEvent {

	private final long bytesTransfered;
	private final String filePath;
	private final FileSystemClient client;
	private final Time duration;

	public Read(FileSystemClient client, Time scheduledTime, Time duration, String filePath, long bytesTransfered) {
		super(client.getHost(), scheduledTime);
		
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
		client.read(filePath, bytesTransfered, getScheduledTime(), duration);
	}

}