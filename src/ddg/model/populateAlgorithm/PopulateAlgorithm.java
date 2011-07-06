package ddg.model.populateAlgorithm;

import java.util.List;
import java.util.Map;

import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;
import ddg.util.FileSizeDistribution;

public interface PopulateAlgorithm {

	/**
	 * @param numFullDSs
	 * @param rlevel
	 * @param dataServers
	 * @param fileDist
	 * @param mapping
	 */
	public void populateNamespace(int numFullDSs, int rlevel,
			List<DataServer> dataServers, FileSizeDistribution fileDist,
			Map<String, ReplicationGroup> mapping);

}
