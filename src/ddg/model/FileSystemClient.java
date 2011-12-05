package ddg.model;

import java.util.PriorityQueue;

import ddg.emulator.event.filesystem.CloseEvent;
import ddg.emulator.event.filesystem.ReadEvent;
import ddg.emulator.event.filesystem.UnlinkEvent;
import ddg.emulator.event.filesystem.WriteEvent;
import ddg.emulator.event.machine.FileSystemActivityEvent;
import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.Time;
import ddg.model.data.ReplicationGroup;

public class FileSystemClient extends EventHandler {

	private final String id;
	private final MetadataServer metadataServer;
	private final Machine machine;

	/**
	 * 
	 * @param scheduler
	 * @param machine
	 * @param metadataServer
	 */
	public FileSystemClient(PriorityQueue<Event> eventsGeneratedBySimulationQueue,
			Machine machine, MetadataServer metadataServer) {

		super(eventsGeneratedBySimulationQueue);

		this.metadataServer = metadataServer;
		this.machine = machine;
		this.id = "client" + machine.bindClient(this) + machine;
	}

	@Override
	public void handleEvent(Event anEvent) {

		String anEventName = anEvent.getName();

		if (anEventName.equals(ReadEvent.EVENT_NAME)) {
			handleRead((ReadEvent) anEvent);
		} else if (anEventName.equals(WriteEvent.EVENT_NAME)) {
			handleWrite((WriteEvent) anEvent);
		} else if(anEventName.equals(CloseEvent.EVENT_NAME)) {
			handleClose((CloseEvent) anEvent);
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
		group.addChangesDuration(writeEvent.getDuration());
		
		Machine primaryDataServerMachine =
			group.getPrimary().getMachine();
		
		sendFSActivity(primaryDataServerMachine, writeEvent.getScheduledTime(), writeEvent.getDuration());
	}
	
	private void sendFSActivity(Machine machine, Time now, Time duration) {
		boolean isLocalFS = getMachine().equals(machine);
		
		FileSystemActivityEvent fsActivity = 
			new FileSystemActivityEvent(machine, now, duration, isLocalFS);

		send(fsActivity);
	}
	
	private void handleClose(CloseEvent closeEvent) {
		String filePath = closeEvent.getFilePath();
		
		metadataServer.closePath(this, filePath, closeEvent.getScheduledTime());		
	}
	
	private void handleUnlink(UnlinkEvent unlinkEvent) {
		String filePath = unlinkEvent.getFilePath();
		
		metadataServer.deletePath(this, filePath, unlinkEvent.getScheduledTime());
	}

	/**
	 * @return the machine
	 */
	public Machine getMachine() {
		return machine;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileSystemClient other = (FileSystemClient) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}