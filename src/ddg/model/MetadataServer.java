package ddg.model;

import java.util.HashMap;
import java.util.Map;

import ddg.model.data.ReplicationGroup;
import ddg.model.placement.DataPlacementAlgorithm;

public class MetadataServer {
	
	private final DataPlacementAlgorithm dataPlacement;

	private final Map<String, ReplicationGroup> files;
	private final Map<String, ReplicationGroup> openFiles;
	
	private final int replicationLevel;


	public MetadataServer(DataPlacementAlgorithm dataPlacementAlgorithm, int replicationLevel) {

		if (dataPlacementAlgorithm == null)
			throw new IllegalArgumentException();
		if(replicationLevel < 1)
			throw new IllegalArgumentException();

		this.dataPlacement = dataPlacementAlgorithm;
		this.files = new HashMap<String, ReplicationGroup>();
		this.openFiles = new HashMap<String, ReplicationGroup>();
		this.replicationLevel = replicationLevel;
	}

	public ReplicationGroup openPath(DDGClient client, String fileName) {

		if (!files.containsKey(fileName)) {
			createFile(fileName, replicationLevel, client);// FIXME externalize
		}

		ReplicationGroup replicationGroup = files.get(fileName);
		openFiles.put(fileName, replicationGroup);

		return replicationGroup;
	}
	
	public void closePath(DDGClient client, String fileName) {
		ReplicationGroup replicationGroup = openFiles.remove(fileName);
		
		if(replicationGroup != null && replicationGroup.isChanged()) {
			throw new RuntimeException("must implement this");
			/*
			 * TODO make MetadataServer implement EventHandler
			 * TODO schedule replica update
			 * TODO implement handleEvent to deal with replicas updates and replicas deletions
			 */
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

}