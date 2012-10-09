package simulation.beefs.event.filesystem;

import simulation.beefs.event.MachineDelaybleEvent;
import simulation.beefs.model.FileSystemClient;
import core.Time;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class Close extends MachineDelaybleEvent {
	
	private final String filePath;
	private final FileSystemClient client;

	public Close(FileSystemClient client, Time scheduledTime, String filePath) {
		super(client.getHost(), scheduledTime);
		
		this.filePath = filePath;
		this.client = client;
	}

	@Override
	public void process() {
		client.close(filePath);
	}

}