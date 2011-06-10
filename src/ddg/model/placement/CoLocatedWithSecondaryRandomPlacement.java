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

import static ddg.model.placement.DataPlacementUtil.chooseRandomDataServers;

import java.util.List;
import java.util.Random;

import ddg.model.DDGClient;
import ddg.model.data.DataServer;
import ddg.util.Pair;

/**
 * TODO make doc
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class CoLocatedWithSecondaryRandomPlacement implements DataPlacementAlgorithm {

	/**
	 * {@inheritDoc}
	 */
	public Pair<DataServer, List<DataServer>> createFile(String fileName, int replicationLevel, List<DataServer> availableDataServers, DDGClient client) {

		List<DataServer> dataServersToRequest = chooseRandomDataServers(availableDataServers, replicationLevel);

		// choose one of the co-located data servers
		List<DataServer> dss = client.getMachine().getDeployedDataServers();
		DataServer coAllocated = dss.get(new Random().nextInt(dss.size()));

		if (dataServersToRequest.contains(coAllocated)) {
			dataServersToRequest.remove(coAllocated);
		} else {
			dataServersToRequest.remove(0);
		}

		return new Pair<DataServer, List<DataServer>>(coAllocated, dataServersToRequest);
	}

}