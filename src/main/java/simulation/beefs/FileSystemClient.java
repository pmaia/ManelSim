package simulation.beefs;

import java.util.PriorityQueue;

import simulation.beefs.event.filesystem.ReadEvent;
import simulation.beefs.event.filesystem.UnlinkEvent;
import simulation.beefs.event.filesystem.WriteEvent;
import simulation.beefs.event.machine.FileSystemActivityEvent;
import core.Event;
import core.EventHandler;
import core.Time;


public class FileSystemClient extends EventHandler {

	private final MetadataServer metadataServer;
	private final Machine machine;
	private final boolean wakeOnLan;

	public FileSystemClient(PriorityQueue<Event> eventsGeneratedBySimulationQueue,
			Machine machine, MetadataServer metadataServer, boolean wakeOnLan) {

		super(eventsGeneratedBySimulationQueue);

		this.metadataServer = metadataServer;
		this.machine = machine;
		this.wakeOnLan = wakeOnLan;
	}
	
	public MetadataServer getMetadataServer() {
		return metadataServer;
	}

	@Override
	public void handleEvent(Event anEvent) {

		String anEventName = null;

		if (anEventName.equals(ReadEvent.EVENT_NAME)) {
			handleRead((ReadEvent) anEvent);
		} else if (anEventName.equals(WriteEvent.EVENT_NAME)) {
			handleWrite((WriteEvent) anEvent);
		} else if(anEventName.equals(UnlinkEvent.EVENT_NAME)) {
			handleUnlink((UnlinkEvent) anEvent);
		} else {
			throw new RuntimeException("Unknown event: " + anEvent);
		} 

	}
	
	private void handleRead(ReadEvent readEvent) {
		String filePath = readEvent.getFilePath();
		ReplicationGroup group = metadataServer.openPath(this, filePath);
		
		Machine primaryDataServerMachine =
			group.getPrimary().getMachine();
		
		sendFSActivity(primaryDataServerMachine, readEvent.getScheduledTime(), readEvent.getDuration());
	}
	
	private void handleWrite(WriteEvent writeEvent) {
		String filePath = writeEvent.getFilePath();
		ReplicationGroup group = metadataServer.openPath(this, filePath);
		group.setChanged(true);
		group.setFileSize(writeEvent.getFileSize());
		
		Machine primaryDataServerMachine =
			group.getPrimary().getMachine();
		
		sendFSActivity(primaryDataServerMachine, writeEvent.getScheduledTime(), writeEvent.getDuration());
	}
	
	private void sendFSActivity(Machine machine, Time now, Time duration) {
		boolean wakeOnLan = false;
		
		if(!getMachine().equals(machine) && this.wakeOnLan) {
			wakeOnLan = true;
		}
			
		FileSystemActivityEvent fsActivity = 
			new FileSystemActivityEvent(machine, now, duration, wakeOnLan);

		send(fsActivity);
	}
	
	private void handleUnlink(UnlinkEvent unlinkEvent) {
		String filePath = unlinkEvent.getFilePath();
		
		metadataServer.deletePath(this, filePath, unlinkEvent.getScheduledTime());
	}

	public Machine getMachine() {
		return machine;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Client deployed on " + machine.getId();
	}

}