package ddg.model;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import ddg.emulator.event.filesystem.DeleteReplicationGroup;
import ddg.emulator.event.filesystem.UpdateReplicationGroup;
import ddg.emulator.event.machine.FileSystemActivityEvent;
import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.Time;
import ddg.kernel.Time.Unit;
import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;
import ddg.model.placement.DataPlacementAlgorithm;

public class MetadataServer extends EventHandler {
	
	private final DataPlacementAlgorithm dataPlacement;
	private final Time timeBeforeDeleteData;
	private final Time timeBeforeUpdateReplicas;

	private final Map<String, ReplicationGroup> files;
	private final Map<String, ReplicationGroup> openFiles;
	private final Map<String, ReplicationGroup> toDelete;
	
	private final int replicationLevel;

	/**
	 * 
	 * @param scheduler
	 * @param dataPlacementAlgorithm
	 * @param replicationLevel
	 * @param timeBeforeDeleteData in seconds
	 * @param timeBeforeUpdateReplicas in seconds
	 */
	public MetadataServer(PriorityQueue<Event> eventsGeneratedBySimulationQueue, 
			DataPlacementAlgorithm dataPlacementAlgorithm, int replicationLevel, 
			long timeBeforeDeleteData, long timeBeforeUpdateReplicas) {
		
		super(eventsGeneratedBySimulationQueue);

		if (dataPlacementAlgorithm == null)
			throw new IllegalArgumentException();
		if(replicationLevel < 1)
			throw new IllegalArgumentException();
		if(timeBeforeDeleteData < 0)
			throw new IllegalArgumentException();
		if(timeBeforeUpdateReplicas < 0)
			throw new IllegalArgumentException();

		this.dataPlacement = dataPlacementAlgorithm;
		this.files = new HashMap<String, ReplicationGroup>();
		this.openFiles = new HashMap<String, ReplicationGroup>();
		this.toDelete = new HashMap<String, ReplicationGroup>();
		this.replicationLevel = replicationLevel;
		this.timeBeforeDeleteData = new Time(timeBeforeDeleteData, Unit.SECONDS);
		this.timeBeforeUpdateReplicas = new Time(timeBeforeUpdateReplicas, Unit.SECONDS);
	}

	public ReplicationGroup openPath(FileSystemClient client, String filePath) {

		if (!files.containsKey(filePath)) {
			createFile(filePath, replicationLevel, client);
		}

		ReplicationGroup replicationGroup = files.get(filePath);
		openFiles.put(filePath, replicationGroup);

		return replicationGroup;
	}
	
	public void closePath(FileSystemClient client, String filePath, Time now) {
		ReplicationGroup replicationGroup = openFiles.remove(filePath);
		
		if(replicationGroup != null) {
			Time noTime = new Time(0, Unit.SECONDS);
			
			if(!noTime.equals(replicationGroup.getTotalChangesDuration())) {
				Time time = now.plus(timeBeforeUpdateReplicas);
				send(new UpdateReplicationGroup(this, time, replicationGroup.getTotalChangesDuration(), filePath));
			}
		}
	}
	
	public void deletePath(FileSystemClient client, String filePath, Time now) {
		ReplicationGroup replicationGroup = files.remove(filePath);
		
		if(replicationGroup != null) {
			toDelete.put(filePath, replicationGroup);
			Time time = now.plus(timeBeforeDeleteData);
			send(new DeleteReplicationGroup(this, time, filePath));
		}
		
	}

	private ReplicationGroup createFile(String filePath, int replicationLevel, FileSystemClient client) {

		if (files.containsKey(filePath)) {
			return null;
		}

		ReplicationGroup replicationGroup = 
			dataPlacement.createFile(client, filePath, replicationLevel);
			
		files.put(filePath, replicationGroup);

		return replicationGroup;
	}

	@Override
	public void handleEvent(Event anEvent) {
		String anEventName = anEvent.getName();

		if (anEventName.equals(DeleteReplicationGroup.EVENT_NAME)) {
			handleDeleteReplicationGroup((DeleteReplicationGroup) anEvent);
		} else if (anEventName.equals(UpdateReplicationGroup.EVENT_NAME)) {
			handleUpdateReplicationGroup((UpdateReplicationGroup) anEvent);
		} else {
			throw new RuntimeException("Unknown event: " + anEvent);
		} 

	}
	
	private void sendFSActivity(Machine machine, Time now, Time duration) {
		FileSystemActivityEvent fsActivity = 
			new FileSystemActivityEvent(machine, now, duration, false);
		
		send(fsActivity);
	}

	private void handleUpdateReplicationGroup(UpdateReplicationGroup anEvent) {
		ReplicationGroup replicationGroup = 
				files.get(anEvent.getFilePath());
		
		if(replicationGroup != null) {
			Time duration = replicationGroup.getTotalChangesDuration();
			
			Machine primaryMachine = replicationGroup.getPrimary().getMachine();
			for(DataServer dataServer : replicationGroup.getSecondaries()) {
				sendFSActivity(primaryMachine, anEvent.getScheduledTime(), duration);
				sendFSActivity(dataServer.getMachine(), anEvent.getScheduledTime(), duration);
			}
		}
	}

	private void handleDeleteReplicationGroup(DeleteReplicationGroup anEvent) {
		ReplicationGroup replicationGroup =
				files.get(anEvent.getFilePath());
		
		if(replicationGroup != null) {
			Time duration = new Time(0, Unit.SECONDS);
			
			sendFSActivity(replicationGroup.getPrimary().getMachine(), anEvent.getScheduledTime(), duration);
			for(DataServer dataServer : replicationGroup.getSecondaries()) {
				sendFSActivity(dataServer.getMachine(), anEvent.getScheduledTime(), duration);
			}
		}
	}

}