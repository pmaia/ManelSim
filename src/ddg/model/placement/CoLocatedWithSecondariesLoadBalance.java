/**
 * Copyright (C) 2009 Universidade Federal de Campina Grande
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ddg.model.placement;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ddg.model.DDGClient;
import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;

/**
 * TODO make doc
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class CoLocatedWithSecondariesLoadBalance implements DataPlacementAlgorithm {
	
	private final Set<DataServer> dataServers;
	
	public CoLocatedWithSecondariesLoadBalance(Set<DataServer> dataServers) {
		this.dataServers = dataServers;
	}

	@Override
	public ReplicationGroup createFile(DDGClient client, String fileName,
			int replicationLevel) {
		
		List<DataServer> secondaries = fromLessToMoreOccupied();

		// choose one of the co-located data servers
		List<DataServer> dss = client.getMachine().getDeployedDataServers();
		DataServer coAllocated = dss.get(new Random().nextInt(dss.size()));

		if (secondaries.contains(coAllocated)) {
			secondaries.remove(coAllocated);
		} else {
			secondaries.remove(secondaries.size() - 1); //FIXME is this else necessary?
		}

		while (secondaries.size() > (replicationLevel - 1)) { //FIXME why - 1? replication level is the number of copies or the primary is considered?
			secondaries.remove(secondaries.size() - 1);
		}

		return new ReplicationGroup(fileName, coAllocated, new HashSet<DataServer>(secondaries));
	}
	

	private List<DataServer> fromLessToMoreOccupied() {

		List<DataServer> copiedList = new LinkedList<DataServer>(dataServers);

		Collections.sort(copiedList);

		return copiedList;
	}

}