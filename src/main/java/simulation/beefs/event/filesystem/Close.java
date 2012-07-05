package simulation.beefs.event.filesystem;

import simulation.beefs.model.FileSystemClient;
import core.Event;
import core.Time;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class Close extends Event {
	
	private final String filePath;
	private final FileSystemClient client;

	public Close(FileSystemClient client, Time scheduledTime, String filePath) {
		super(scheduledTime);
		
		this.filePath = filePath;
		this.client = client;
	}

	@Override
	public void process() {
		client.close(filePath);
	}

}