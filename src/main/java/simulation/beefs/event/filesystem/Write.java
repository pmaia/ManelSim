package simulation.beefs.event.filesystem;

import simulation.beefs.event.MachineDelaybleEvent;
import simulation.beefs.model.FileSystemClient;
import core.Time;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class Write extends MachineDelaybleEvent {

	private final long bytesTransfered;
	private final long fileSize;
	private final String filePath;
	private final Time duration;
	private final FileSystemClient client;

	public Write(FileSystemClient client, Time scheduledTime, Time duration, String filePath, 
			long bytesTransfered, long fileSize) {
		
		super(client.getHost(), scheduledTime);
		
		this.client = client;
		this.bytesTransfered = bytesTransfered;
		this.fileSize = fileSize;
		this.duration = duration;
		this.filePath = filePath;
	}

	@Override
	public String toString() {
		return "write\t" + getScheduledTime() + "\t" + filePath + "\t" + bytesTransfered + "\t" + fileSize;
	}

	@Override
	public void process() {
		client.write(filePath, fileSize, bytesTransfered, getScheduledTime(), duration);
	}
}