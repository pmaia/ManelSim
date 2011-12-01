package ddg.model;

import java.util.HashMap;
import java.util.Map;

import ddg.emulator.event.filesystem.DeleteReplicationGroup;
import ddg.emulator.event.filesystem.UpdateReplicationGroup;
import ddg.emulator.event.machine.UserActivityEvent;
import ddg.kernel.Event;
import ddg.kernel.EventHandler;
import ddg.kernel.EventScheduler;
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
	public MetadataServer(EventScheduler scheduler, 
			DataPlacementAlgorithm dataPlacementAlgorithm, int replicationLevel, 
			long timeBeforeDeleteData, long timeBeforeUpdateReplicas) {
		
		super(scheduler);

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

	public ReplicationGroup openPath(DDGClient client, String filePath) {

		if (!files.containsKey(filePath)) {
			createFile(filePath, replicationLevel, client);
		}

		ReplicationGroup replicationGroup = files.get(filePath);
		openFiles.put(filePath, replicationGroup);

		return replicationGroup;
	}
	
	public void closePath(DDGClient client, String filePath) {
		ReplicationGroup replicationGroup = openFiles.remove(filePath);
		
		if(replicationGroup != null && replicationGroup.isChanged()) {
			Time time = getScheduler().now().plus(timeBeforeUpdateReplicas);
			send(new UpdateReplicationGroup(this, time, filePath));
		}
	}
	
	public void deletePath(DDGClient client, String filePath) {
		ReplicationGroup replicationGroup = files.remove(filePath);
		
		if(replicationGroup != null) {
			toDelete.put(filePath, replicationGroup);
			Time time = getScheduler().now().plus(timeBeforeDeleteData);
			send(new DeleteReplicationGroup(this, time, filePath));
		}
		
	}

	private ReplicationGroup createFile(String filePath, int replicationLevel, DDGClient client) {

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

	private void handleUpdateReplicationGroup(UpdateReplicationGroup anEvent) {
		ReplicationGroup replicationGroup = 
				files.get(anEvent.getFilePath());
		
		if(replicationGroup != null) {
			Machine primaryMachine = replicationGroup.getPrimary().getMachine();
			for(DataServer dataServer : replicationGroup.getSecondaries()) {
				wakeUpIfSleeping(primaryMachine);
				wakeUpIfSleeping(dataServer.getMachine());
			}
		}
	}

	private void handleDeleteReplicationGroup(DeleteReplicationGroup anEvent) {
		ReplicationGroup replicationGroup =
				files.get(anEvent.getFilePath());
		
		if(replicationGroup != null) {
			wakeUpIfSleeping(replicationGroup.getPrimary().getMachine());
			for(DataServer dataServer : replicationGroup.getSecondaries()) {
				wakeUpIfSleeping(dataServer.getMachine());
			}
		}
	}

	private void wakeUpIfSleeping(Machine machine) {
		if(machine.isSleeping()) {
			machine.handleEvent(new UserActivityEvent(machine, getScheduler().now(), true));
		}
	}
}