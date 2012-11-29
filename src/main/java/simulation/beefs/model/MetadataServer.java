package simulation.beefs.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.beefs.event.filesystem.DeleteFileReplicas;
import simulation.beefs.event.filesystem.UpdateFileReplicas;
import simulation.beefs.placement.DataPlacementAlgorithm;
import core.EventScheduler;
import core.Time;

/**
 * @author Patrick Maia
 */
public class MetadataServer {
	
	private static final Logger logger = LoggerFactory
			.getLogger(MetadataServer.class);
	
	private final DataPlacementAlgorithm dataPlacement;
	
	private final int replicationLevel;
	
	private final Time timeToCoherence;
	
	private final Time timeToDelete;

	private final Map<String, ReplicatedFile> files = new HashMap<String, ReplicatedFile>();

	// Patrick: I'm considering that there is just one DataServer per machine.
	private final Map<String, DataServer> dataServerByHost = new HashMap<String, DataServer>();
	
	public MetadataServer(Set<ReplicatedFile> namespace, Set<DataServer> dataServers, String dataPlacementStrategy, 
			int replicationLevel, Time timeToCoherence, Time timeToDelete) {
		
		//didn't like it. add a ds to a machine.
		for(DataServer dataServer : dataServers) {
			dataServerByHost.put(dataServer.getHost().getName(), dataServer);
		}
		
		//FIXME: receive the placement alg. as arg
		this.dataPlacement = DataPlacementAlgorithm.newDataPlacementAlgorithm(dataPlacementStrategy, dataServers);
		this.replicationLevel = replicationLevel;
		this.timeToCoherence = timeToCoherence;
		this.timeToDelete = timeToDelete;
		
		for (ReplicatedFile replicatedFile : namespace) {
			files.put(replicatedFile.getFullPath(), replicatedFile);
		}
	}
	
	public MetadataServer(Set<DataServer> dataServers, String dataPlacementStrategy, 
			int replicationLevel, Time timeToCoherence, Time timeToDelete) {
		
		this(new HashSet<ReplicatedFile>(), dataServers, dataPlacementStrategy, replicationLevel,
				timeToCoherence, timeToDelete);
	}
	
	public void close(String filePath) {
		ReplicatedFile file = files.get(filePath);
		
		if(file != null && !file.areReplicasConsistent() && file.getSecondaries().size() > 0) {
			Time now = EventScheduler.now();
			EventScheduler.schedule(new UpdateFileReplicas(now.plus(timeToCoherence), filePath));
		}
	}
	
	public void delete(String filePath) {
		ReplicatedFile file = files.remove(filePath);
		//FIXME Patrick: tenho que fazer aqui em file.getPrimary() o mesmo que eu fizer em DeleteFileReplicas.process()
		if(file != null && file.getSecondaries().size() > 0) {
			Time now = EventScheduler.now();
			EventScheduler.schedule(new DeleteFileReplicas(now.plus(timeToDelete), file));
		}
	}

	public ReplicatedFile createOrOpen(FileSystemClient client, String path) {
		ReplicatedFile theFile = open(path);
		
		if(theFile == null) {
			theFile = createFile(client, path);
		}
		
		return theFile;
	}
	
	public ReplicatedFile open(String path) {
		return files.get(path);
	}
	
	private ReplicatedFile createFile(FileSystemClient client, String fullpath) {
		
		logger.info("client={} request file creation path={}", 
				client.toString(), fullpath);
		DataServer primary = getDataServer(client.getHost().getName());
		ReplicatedFile newFile = dataPlacement.createFile(primary, fullpath, replicationLevel);
		
		files.put(fullpath, newFile);
		
		return newFile;
	}

	public List<DataServer> dataServers() {
		return new LinkedList<DataServer>(dataServerByHost.values());
	}
	
	public DataServer getDataServer(String host) {
		//FIXME: didn't like it
		return dataServerByHost.get(host);
	}

}