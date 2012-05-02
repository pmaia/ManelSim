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
package model.placement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.RandomUtil;

import model.data.DataServer;


/**
 * TODO make doc
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public class DataPlacementUtil {

	/**
	 * @param availableServers
	 * @param numberOfWantedServers
	 * @return
	 */
	public static Set<DataServer> chooseRandomDataServers(Set<DataServer> availableServers, int numberOfWantedServers) {
		
		List<DataServer> availableServersAsList = new ArrayList<DataServer>();
		availableServersAsList.addAll(availableServers);

		int numberOfSelectedDataServers = 
			(availableServers.size() > numberOfWantedServers) ? numberOfWantedServers : availableServers.size();

		Set<DataServer> randomDataServers = new HashSet<DataServer>();

		List<Integer> randomList = RandomUtil.random(availableServers.size());

		for (int i = 0; i < numberOfSelectedDataServers; i++) {
			randomDataServers.add(availableServersAsList.get(randomList.get(i)));
		}

		return randomDataServers;
	}

}
