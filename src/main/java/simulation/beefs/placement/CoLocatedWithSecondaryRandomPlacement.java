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
package simulation.beefs.placement;

import static simulation.beefs.placement.DataPlacementUtil.chooseRandomDataServers;

import java.util.HashSet;
import java.util.Set;

import simulation.beefs.model.DataServer;
import simulation.beefs.model.ReplicatedFile;

/**
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class CoLocatedWithSecondaryRandomPlacement extends DataPlacementAlgorithm {
	
	public CoLocatedWithSecondaryRandomPlacement(Set<DataServer> dataServers) {
		super(dataServers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReplicatedFile createFile(DataServer primary, String fileName, 
			int replicationLevel) {
		
		Set<DataServer> tmpAvailableDSs = new HashSet<DataServer>(dataServers);
		tmpAvailableDSs.remove(primary);
		
		Set<DataServer> secondaries = chooseRandomDataServers(tmpAvailableDSs, replicationLevel);
		
		return new ReplicatedFile(fileName, replicationLevel, primary, secondaries);
	}

}