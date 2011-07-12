package ddg.model.populateAlgorithm;

import java.util.List;

import ddg.model.DDGClient;
import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;
import ddg.model.placement.DataPlacementAlgorithm;
import ddg.util.FileSizeDistribution;
import ddg.util.Pair;

/**
 * @author manel
 * 
 */
public abstract class AbstractPopulateAlgorithm implements PopulateAlgorithm {

	/**
	 * @param placement
	 * @param fileName
	 * @param replicationLevel
	 * @param nonFullDataServers
	 * @param client
	 * @param fileSizeDistribution
	 * @return
	 */
	protected ReplicationGroup createReplicationGroup(
			DataPlacementAlgorithm placement, String fileName,
			int replicationLevel, List<DataServer> nonFullDataServers,
			DDGClient client, FileSizeDistribution fileSizeDistribution) {

		Pair<DataServer, List<DataServer>> group = placement.createFile(
				fileName, replicationLevel, nonFullDataServers, client);

		DataServer primaryDataServer = group.first;
		long fileSize = (long) Math.min(fileSizeDistribution.nextSampleSize(),
				primaryDataServer.getAvailableDiskSize());

		return new ReplicationGroup(fileName, fileSize, primaryDataServer, group.second);
	}
}
