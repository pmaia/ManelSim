package simulation.beefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulation.beefs.event.filesystem.DeleteReplicationGroup;
import simulation.beefs.event.filesystem.UpdateReplicationGroup;
import simulation.beefs.event.machine.FileSystemActivityEvent;
import simulation.beefs.placement.DataPlacementAlgorithm;
import core.EventScheduler;
import core.Time;
import core.Time.Unit;


public class MetadataServer {
	
	private final DataPlacementAlgorithm dataPlacement;
	private final Time timeBeforeDeleteData;
	private final Time timeBeforeUpdateReplicas;

	private final Map<String, ReplicationGroup> files;
	private final Map<String, ReplicationGroup> openFiles;
	private final Map<String, ReplicationGroup> toDelete;
	
	private final int replicationLevel;
	private final boolean wakeOnLan;

	public MetadataServer(DataPlacementAlgorithm dataPlacementAlgorithm, int replicationLevel, 
			long timeBeforeDeleteData, long timeBeforeUpdateReplicas, boolean wakeOnLan) {
		
		if (dataPlacementAlgorithm == null)
			throw new IllegalArgumentException();
		if(replicationLevel < 1) //FIXME replication level 0 must be possible
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
		this.wakeOnLan = wakeOnLan;
	}

	public ReplicationGroup openPath(FileSystemClient client, String filePath) {

		if (!files.containsKey(filePath)) {
			createFile(filePath, replicationLevel, client);
		}

		ReplicationGroup replicationGroup = files.get(filePath);
		openFiles.put(filePath, replicationGroup);

		return replicationGroup;
	}
	
	public void closePath(String filePath, Time now) {
		ReplicationGroup replicationGroup = openFiles.remove(filePath);
		
		if(replicationGroup != null && replicationGroup.isChanged()) {
			Time time = now.plus(timeBeforeUpdateReplicas);
			
			EventScheduler.schedule(new UpdateReplicationGroup(this, time, filePath));
		}
	}
	
	public void deletePath(FileSystemClient client, String filePath, Time now) {
		ReplicationGroup replicationGroup = files.remove(filePath);
		
		if(replicationGroup != null) {
			toDelete.put(filePath, replicationGroup);
			Time time = now.plus(timeBeforeDeleteData);
			EventScheduler.schedule(new DeleteReplicationGroup(this, time, filePath));
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

	private void sendFSActivity(Machine machine, Time now, Time duration, boolean wakeOnLan) {
		FileSystemActivityEvent fsActivity = 
			new FileSystemActivityEvent(machine, now, duration, wakeOnLan);
		
		send(fsActivity);
	}

	private void handleUpdateReplicationGroup(UpdateReplicationGroup anEvent) {
		ReplicationGroup replicationGroup = 
				files.get(anEvent.getFilePath());
		
		if(replicationGroup != null) {
			Time duration = anEvent.getDuration();

			Machine primaryMachine = replicationGroup.getPrimary().getMachine();
			
			for(DataServer dataServer : replicationGroup.getSecondaries()) {
				sendFSActivity(primaryMachine, anEvent.getScheduledTime(), duration, true);
				
				Machine secondaryMachine = dataServer.getMachine();
				if(!wakeOnLan && !secondaryMachine.isAwake()) {
					List<DataServer> exceptions = new ArrayList<DataServer>();
					exceptions.add(replicationGroup.getPrimary());
					DataServer replacement = dataPlacement.giveMeASingleDataServer(exceptions);
					
					if(replacement != null) {
						sendFSActivity(replacement.getMachine(), anEvent.getScheduledTime(), duration, false);
						replicationGroup.replaceSecondaryDataServer(dataServer, replacement);
					} else {
						sendFSActivity(dataServer.getMachine(), anEvent.getScheduledTime(), duration, true);
					}
				} else {
					sendFSActivity(dataServer.getMachine(), anEvent.getScheduledTime(), duration, wakeOnLan);
				}
			}
		}
	}

	private void handleDeleteReplicationGroup(DeleteReplicationGroup anEvent) {
		ReplicationGroup replicationGroup =
				toDelete.remove(anEvent.getFilePath());
		
		if(replicationGroup != null && wakeOnLan) {
			Time duration = new Time(0, Unit.SECONDS);
			
			sendFSActivity(replicationGroup.getPrimary().getMachine(), anEvent.getScheduledTime(), duration, true);
			for(DataServer dataServer : replicationGroup.getSecondaries()) {
				sendFSActivity(dataServer.getMachine(), anEvent.getScheduledTime(), duration, true);
			}
		}
	}

}