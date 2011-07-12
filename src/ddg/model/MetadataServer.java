package ddg.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ddg.emulator.events.metadataServerEvents.ReplicateAllEvent;
import ddg.kernel.JEEvent;
import ddg.kernel.JEEventHandler;
import ddg.kernel.JEEventScheduler;
import ddg.kernel.JETime;
import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;
import ddg.model.placement.DataPlacementAlgorithm;
import ddg.model.populateAlgorithm.PopulateAlgorithm;
import ddg.util.FileSizeDistribution;
import ddg.util.Pair;

/**
 * Herald's emulation abstraction. This code does not match exactly with
 * Herald's protocol
 * 
 * @author Thiago Emmanuel Pereira da Cunha Silva - thiagoepdc@lsd.ufcg.edu.br
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class MetadataServer extends JEEventHandler {

	private final DataPlacementAlgorithm dataPlacement;
	private final List<DataServer> availableDataServers;
	private final FileSizeDistribution fileSizeDistribution;

	private final Map<String, ReplicationGroup> files;
	private final Map<Integer, ReplicationGroup> openFiles;
	private final PopulateAlgorithm populateAlgorithm;

	public final static int ONE_DAY = 1000 * 60 * 60 * 24;

	/**
	 * Default constructor using fields.
	 * 
	 * @param scheduler
	 * @param dataServers
	 * @param dataPlacementAlgorithm
	 * @param fileSizeDistribution
	 * @param popAlgorithm
	 *            TODO
	 */
	public MetadataServer(JEEventScheduler scheduler,
			List<DataServer> dataServers,
			DataPlacementAlgorithm dataPlacementAlgorithm,
			FileSizeDistribution fileSizeDistribution,
			PopulateAlgorithm popAlgorithm) {

		super(scheduler);

		if (dataServers == null)
			throw new IllegalArgumentException();
		if (dataServers.isEmpty())
			throw new IllegalArgumentException();
		if (dataPlacementAlgorithm == null)
			throw new IllegalArgumentException();
		if (fileSizeDistribution == null)
			throw new IllegalArgumentException();
		if (popAlgorithm == null)
			throw new IllegalArgumentException();

		this.dataPlacement = dataPlacementAlgorithm;
		this.populateAlgorithm = popAlgorithm;
		this.availableDataServers = new LinkedList<DataServer>(dataServers);

		this.files = new HashMap<String, ReplicationGroup>();
		this.fileSizeDistribution = fileSizeDistribution;
		this.openFiles = new HashMap<Integer, ReplicationGroup>();
		
		scheduler.queue_event(new ReplicateAllEvent(this, scheduler.now().plus(new JETime(5 * JETime.MINUTE))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kernel.JEEventHandler#event_handler(kernel.JEEvent)
	 */
	@Override
	public void handleEvent(JEEvent anEvent) {

		if (anEvent.getName().equals(ReplicateAllEvent.EVENT_NAME)) {
			for(String fileName : files.keySet()) {
				ReplicationGroup aReplicationGroup = files.get(fileName);
				if(aReplicationGroup.isDirty()) {
					aReplicationGroup.updateReplicas();
				}
			}
			
			super.getScheduler().queue_event(
					new ReplicateAllEvent(
							this, super.getScheduler().now().plus(new JETime(5 * JETime.MINUTE))));
		} else {
			throw new RuntimeException();
		}
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
			createFile(fileName, 2, client);// FIXME externalize
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
	 * @param numOfFullDataServers
	 * @param replicationLevel
	 * @param dataServers
	 */
	public void populateNamespace(int numOfFullDataServers,
			int replicationLevel, List<DataServer> dataServers) {
		populateAlgorithm.populateNamespace(numOfFullDataServers,
				replicationLevel, dataServers, fileSizeDistribution, files);
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