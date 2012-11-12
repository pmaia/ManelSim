/**
 * 
 */
package simulation.beefs.event.filesystem;

import simulation.beefs.model.FileSystemClient;
import simulation.beefs.userMigration.UserMigrationAlgorithm;
import core.Event;
import core.EventSource;

/**
 * This {@link EventSource} implementation allows changing the {@link FileSystemClient}
 * at runtime. We use it to emulate mobile clients --- those that change their base
 * machine. 
 *  
 * @author manel
 */
public class MobileClientFSTraceEventSource implements EventSource {
	
	private final UserMigrationAlgorithm migrationAlgorithm;
	private final FileSystemTraceEventSource fileSystemTraceEventSource;

	public MobileClientFSTraceEventSource(UserMigrationAlgorithm migrationAlgorithm,
			FileSystemTraceEventSource fileSystemTraceEventSource) {
		
		this.migrationAlgorithm = migrationAlgorithm;
		this.fileSystemTraceEventSource = fileSystemTraceEventSource;
	}
	

	/* (non-Javadoc)
	 * @see core.EventSource#getNextEvent()
	 */
	@Override
	public Event getNextEvent() {
		
		Event newEvent = this.fileSystemTraceEventSource.getNextEvent();
		FileSystemClient newClient = migrationAlgorithm.baseClient(newEvent.getScheduledTime());
		//I dont't want to touch the scheduler, so we know now() just after event create event -- we can
		//migrate with some delay (by one event) after migrationdelay.
		this.fileSystemTraceEventSource.setClient(newClient);
		
		return newEvent; 
	}

}