package simulation.beefs.event.filesystem;

import simulation.beefs.event.MachineDelaybleEvent;
import simulation.beefs.model.FileSystemClient;
import core.Time;

/**
 *
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class Unlink extends MachineDelaybleEvent {
	
	private final String filePath;
	private final FileSystemClient client;
	
	public Unlink(FileSystemClient client, Time aScheduledTime, String filePath) {
		super(client.getHost(), aScheduledTime);
		
		this.client = client;
		this.filePath = filePath;
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	
	@Override
	public String toString() {
		return "unlink\t" + getScheduledTime() + "\t" + filePath;
	}

	@Override
	public void process() {
		client.delete(filePath);
	}

}
