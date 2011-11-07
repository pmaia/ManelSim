package ddg.model;

import java.util.HashMap;
import java.util.Map;

import ddg.model.data.ReplicationGroup;
import ddg.model.placement.DataPlacementAlgorithm;

public class MetadataServer {
	
	private final DataPlacementAlgorithm dataPlacement;

	private final Map<String, ReplicationGroup> files;
	private final Map<String, ReplicationGroup> openFiles;
	private final Map<String, ReplicationGroup> deletePending;
	
	private final int replicationLevel;


	public MetadataServer(DataPlacementAlgorithm dataPlacementAlgorithm, int replicationLevel) {

		if (dataPlacementAlgorithm == null)
			throw new IllegalArgumentException();
		if(replicationLevel < 1)
			throw new IllegalArgumentException();

		this.dataPlacement = dataPlacementAlgorithm;
		this.files = new HashMap<String, ReplicationGroup>();
		this.openFiles = new HashMap<String, ReplicationGroup>();
		this.deletePending = new HashMap<String, ReplicationGroup>();
		this.replicationLevel = replicationLevel;
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
			throw new RuntimeException("must implement this");
			/*
			 * TODO make MetadataServer implement EventHandler
			 * TODO schedule replica update
			 * TODO implement handleEvent to deal with replicas updates and replicas deletions
			 */
		}
	}
	
	public void deletePath(DDGClient client, String filePath) {
		ReplicationGroup replicationGroup = files.remove(filePath);
		
		if(replicationGroup != null) {
			deletePending.put(filePath, replicationGroup);
		}
		/*
		 * TODO schedule a DeleteReplicationGroup task 
		 */
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

}