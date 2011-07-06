package ddg.model.populateAlgorithm;

import java.util.List;
import java.util.Map;

import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;
import ddg.util.FileSizeDistribution;

/**
 * @author manel
 * 
 */
public class NOPAlgorithm extends AbstractPopulateAlgorithm implements
		PopulateAlgorithm {

	/*
	 * (non-Javadoc)
	 * 
	 * @see ddg.model.populateAlgorithm.PopulateAlgorithm#populateNamespace(int,
	 * int, java.util.List, ddg.util.FileSizeDistribution, java.util.Map)
	 */
	@Override
	public void populateNamespace(int numFullDSs, int rlevel,
			List<DataServer> dataServers, FileSizeDistribution fileDist,
			Map<String, ReplicationGroup> mapping) {
	}

}
