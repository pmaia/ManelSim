package simulation.beefs.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	private final Map<String, ReplicatedFile> files = new HashMap<String, ReplicatedFile>();

	// Patrick: I'm considering that there is just one DataServer per machine.
	private final Map<String, DataServer> dataServerByHost = new HashMap<String, DataServer>();

	public MetadataServer(Set<DataServer> dataServers, String dataPlacementStrategy, 
			int replicationLevel, Time timeToCoherence) {
		
		for(DataServer dataServer : dataServers) {
			dataServerByHost.put(dataServer.getHost(), dataServer);
		}
		
		this.dataPlacement = DataPlacementAlgorithm.newDataPlacementAlgorithm(dataPlacementStrategy, dataServers);
		this.replicationLevel = replicationLevel;
		this.timeToCoherence = timeToCoherence;
	}
	
	public void close(String filePath) {
		ReplicatedFile file = files.get(filePath);
		
		if(file != null && !file.areReplicasConsistent() && file.getSecondaries().size() > 0) {
			Time now = EventScheduler.now();
			EventScheduler.schedule(new UpdateFileReplicas(now.plus(timeToCoherence), filePath));
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
		return dataServerByHost.get(host);
	}

}
