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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import ddg.model.FileSystemClient;
import ddg.model.data.DataServer;
import ddg.model.data.ReplicationGroup;

/**
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class CoLocatedWithSecondaryRandomPlacement implements DataPlacementAlgorithm {
	
	private Set<DataServer> dataServers;
	
	public CoLocatedWithSecondaryRandomPlacement(Set<DataServer> dataServers) {
		this.dataServers = dataServers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReplicationGroup createFile(FileSystemClient client, String fileName, int replicationLevel) {
		
		DataServer primary = null;
		Set<DataServer> secondaries;
		
		Set<DataServer> colocatedDataServers = 
			client.getMachine().getDeployedDataServers();
		
		if(!colocatedDataServers.isEmpty()) {
			Set<DataServer> copyOfAvailableDataServers = 
				new HashSet<DataServer>(dataServers);
			
			copyOfAvailableDataServers.removeAll(colocatedDataServers);

			secondaries = 
				chooseRandomDataServers(copyOfAvailableDataServers, replicationLevel - 1);
			// choose one of the co-located data servers
			int theOne = new Random().nextInt(colocatedDataServers.size());
			int i = 1;
			for(DataServer dataServer : colocatedDataServers) {
				if(i++ >= theOne) {
					primary = dataServer;
				}
			}
		} else {
			
			secondaries = new HashSet<DataServer>();
			
			for(DataServer dataServer : chooseRandomDataServers(dataServers, replicationLevel)) {
				if(primary == null) {
					primary = dataServer;
				} else {
					secondaries.add(dataServer);
				}
			}
			
		}

		return new ReplicationGroup(fileName, primary, secondaries);
	}

}