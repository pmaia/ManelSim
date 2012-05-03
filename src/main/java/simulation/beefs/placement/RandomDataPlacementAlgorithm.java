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
import java.util.List;
import java.util.Set;

import simulation.beefs.DataServer;
import simulation.beefs.FileSystemClient;
import simulation.beefs.ReplicationGroup;




/**
 * 
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class RandomDataPlacementAlgorithm implements DataPlacementAlgorithm {
	
	private final Set<DataServer> dataServers;
	
	public RandomDataPlacementAlgorithm(Set<DataServer> dataServers) {
		this.dataServers = dataServers;
	}

	@Override
	public ReplicationGroup createFile(FileSystemClient client, String fileName,
			int replicationLevel) {
		
		Set<DataServer> choosenDataServes = 
			chooseRandomDataServers(dataServers, replicationLevel);
		
		DataServer primary = null;
		Set<DataServer> secondaries = new HashSet<DataServer>();
		
		for(DataServer dataServer : choosenDataServes) {
			if(primary == null) {
				primary = dataServer;
			} else {
				secondaries.add(dataServer);
			}
		}
		
		return new ReplicationGroup(fileName, primary, secondaries);
		
	}

	@Override
	public DataServer giveMeASingleDataServer(List<DataServer> exceptions) {
		throw new UnsupportedOperationException("When this explodes in someone's face it's time to become supported");
	}
	
}