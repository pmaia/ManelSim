package ddg.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;
import ddg.model.placement.DataPlacementAlgorithm;
import ddg.util.FileSizeDistribution;
import ddg.util.Pair;

public class MetadataServer {
	
	private final DataPlacementAlgorithm dataPlacement;
	private final List<DataServer> availableDataServers;
	private final FileSizeDistribution fileSizeDistribution;

	private final Map<String, ReplicationGroup> files;
	private final Map<Integer, ReplicationGroup> openFiles;
	
	private final int replicationLevel;

	public final static int ONE_DAY = 1000 * 60 * 60 * 24;

	/**
	 * @param dataServers
	 * @param dataPlacementAlgorithm
	 * @param replicationLevel
	 * @param fileSizeDistribution
	 */
	public MetadataServer(List<DataServer> dataServers, DataPlacementAlgorithm dataPlacementAlgorithm, 
			int replicationLevel, FileSizeDistribution fileSizeDistribution) {

		if (dataServers == null)
			throw new IllegalArgumentException();
		if (dataServers.isEmpty())
			throw new IllegalArgumentException();
		if (dataPlacementAlgorithm == null)
			throw new IllegalArgumentException();
		if (fileSizeDistribution == null)
			throw new IllegalArgumentException();
		if(replicationLevel < 1)
			throw new IllegalArgumentException();

		this.dataPlacement = dataPlacementAlgorithm;
		this.availableDataServers = new LinkedList<DataServer>(dataServers);

		this.files = new HashMap<String, ReplicationGroup>();
		this.fileSizeDistribution = fileSizeDistribution;
		this.openFiles = new HashMap<Integer, ReplicationGroup>();
		
		this.replicationLevel = replicationLevel;
	}

	/**
	 * @param fileName
	 * @param fileDescriptor
	 * @param client
	 * @return
	 */
	public ReplicationGroup openPath(String fileName, Integer fileDescriptor,
			DDGClient client) {

		if (!files.containsKey(fileName)) {
			createFile(fileName, replicationLevel, client);// FIXME externalize
		}

		ReplicationGroup replicationGroup = files.get(fileName);
		openFiles.put(fileDescriptor, replicationGroup);// FIXME do release
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

	/**
	 * @param placement
	 * @param fileName
	 * @param replicationLevel
	 * @param nonFullDataServers
	 * @param client
	 * @return
	 */
	private ReplicationGroup createReplicationGroup(
			DataPlacementAlgorithm placement, String fileName,
			int replicationLevel, List<DataServer> nonFullDataServers,
			DDGClient client) {

		Pair<DataServer, List<DataServer>> group = placement.createFile(
				fileName, replicationLevel, nonFullDataServers, client);

		DataServer primaryDataServer = group.first;
		long fileSize = (long) Math.min(fileSizeDistribution.nextSampleSize(),
				primaryDataServer.getAvailableDiskSize());

		return new ReplicationGroup(fileName, fileSize, primaryDataServer, group.second);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "herald";
	}

	/**
	 * @param ddgClient
	 * @param fileName
	 * @param offset
	 * @param length
	 */
	public void write(DDGClient ddgClient, String fileName, long offset,
			long length) {
		ReplicationGroup replicationGroup = files.get(fileName);
		replicationGroup.write(fileName, offset, length, ddgClient);
	}

	/**
	 * @param ddgClient
	 * @param fileName
	 * @param offset
	 * @param size
	 */
	public void read(DDGClient ddgClient, String fileName, long offset,
			long size) {
		ReplicationGroup replicationGroup = files.get(fileName);
		replicationGroup.read(fileName, offset, size, ddgClient);
	}

}