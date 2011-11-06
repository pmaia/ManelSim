package ddg.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;
import ddg.model.placement.DataPlacementAlgorithm;
import ddg.util.Pair;

public class MetadataServer {
	
	private final DataPlacementAlgorithm dataPlacement;
	private final List<DataServer> availableDataServers;

	private final Map<String, ReplicationGroup> files;
	private final Map<String, ReplicationGroup> openFiles;
	
	private final int replicationLevel;


	public MetadataServer(List<DataServer> dataServers, DataPlacementAlgorithm dataPlacementAlgorithm, int replicationLevel) {

		if (dataServers == null)
			throw new IllegalArgumentException();
		if (dataServers.isEmpty())
			throw new IllegalArgumentException();
		if (dataPlacementAlgorithm == null)
			throw new IllegalArgumentException();
		if(replicationLevel < 1)
			throw new IllegalArgumentException();

		this.dataPlacement = dataPlacementAlgorithm;
		this.availableDataServers = new LinkedList<DataServer>(dataServers);

		this.files = new HashMap<String, ReplicationGroup>();
		this.openFiles = new HashMap<String, ReplicationGroup>();
		
		this.replicationLevel = replicationLevel;
	}

	/**
	 * @param fileName
	 * @param fileDescriptor
	 * @param client
	 * @return
	 */
	public ReplicationGroup openPath(String fileName, DDGClient client) {

		if (!files.containsKey(fileName)) {
			createFile(fileName, replicationLevel, client);// FIXME externalize
		}

		ReplicationGroup replicationGroup = files.get(fileName);
		openFiles.put(fileName, replicationGroup);// FIXME do release
														// memory leak

		return replicationGroup;
	}

	/**
	 * @param fileDescriptor
	 * @return
	 */
	public ReplicationGroup lookupReplicationGroup(int fileDescriptor) {

		ReplicationGroup group = openFiles.get(fileDescriptor);

		if (group == null) {
			throw new RuntimeException("The file descriptor: " + fileDescriptor
					+ " is not open on this client: " + this
					+ " Open descriptors: " + openFiles.keySet());
		}

		return group;
	}

	private ReplicationGroup createReplicationGroup(
			DataPlacementAlgorithm placement, String fileName,
			int replicationLevel, List<DataServer> nonFullDataServers,
			DDGClient client) {

		Pair<DataServer, List<DataServer>> group = 
			placement.createFile(fileName, replicationLevel, nonFullDataServers, client);

		DataServer primaryDataServer = group.first;

//		return new ReplicationGroup(fileName, primaryDataServer, group.second);
		return null;
	}

	/**
	 * @param fileName
	 * @param fileSize
	 * @param replicationLevel
	 * @param client
	 * @return
	 */
	private ReplicationGroup createFile(String fileName, int replicationLevel,
			DDGClient client) {

		if (files.containsKey(fileName)) {
			return null;
		}

		ReplicationGroup replicationGroup = createReplicationGroup(
				dataPlacement, fileName, replicationLevel,
				this.availableDataServers, client);
		files.put(fileName, replicationGroup);

		return replicationGroup;
	}

}