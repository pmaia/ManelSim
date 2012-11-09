package simulation.beefs.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import simulation.beefs.event.filesystem.DeleteFileReplicas;
import simulation.beefs.event.filesystem.UpdateFileReplicas;
import simulation.beefs.placement.DataPlacementAlgorithm;
import core.EventScheduler;
import core.Time;

/**
 * @author Patrick Maia
 */
public class MetadataServer {
	
	private final DataPlacementAlgorithm dataPlacement;
	
	private final int replicationLevel;
	
	private final Time timeToCoherence;
	
	private final Time timeToDelete;

	private final Map<String, ReplicatedFile> files = new HashMap<String, ReplicatedFile>();

	// Patrick: I'm considering that there is just one DataServer per machine.
	private final Map<String, DataServer> dataServerByHost = new HashMap<String, DataServer>();

	public MetadataServer(Set<DataServer> dataServers, String dataPlacementStrategy, 
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
		ReplicatedFile theFile = files.get(path);
		
		if(theFile == null) {
			theFile = createFile(client, path);
		}
		
		return theFile;
	}

	private ReplicatedFile createFile(FileSystemClient client, String fullpath) {
		ReplicatedFile newFile = dataPlacement.createFile(client, fullpath, replicationLevel);
		
		files.put(fullpath, newFile);
		
		return newFile;
	}
	
	public DataServer getDataServer(String host) {
		//FIXME: didn't like it
		return dataServerByHost.get(host);
	}

}
