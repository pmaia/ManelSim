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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import simulation.beefs.model.DataServer;

/**
 * @author thiagoepdc - thiagoepdc@lsd.ufcg.edu.br
 */
public class DataPlacementUtil {
	
	public static Set<DataServer> filter(double minAvailableSpace, Iterable<DataServer> servers) {
		
		Set<DataServer> response = new HashSet<DataServer>();
		for (DataServer dataServer : servers) {
			
			if (dataServer.availableSpace() >= minAvailableSpace) {
				response.add(dataServer);
			}
		}
		return response;
	}

	public static Set<DataServer> chooseRandomDataServers(Set<DataServer> availableServers, int numberOfWantedServers) {
		
		List<DataServer> availableServersAsList = new ArrayList<DataServer>();
		availableServersAsList.addAll(availableServers);

		int numberOfSelectedDataServers = 
			(availableServers.size() > numberOfWantedServers) ? numberOfWantedServers : availableServers.size();

		Set<DataServer> randomDataServers = new HashSet<DataServer>();

		List<Integer> randomList = random(availableServers.size());

		for (int i = 0; i < numberOfSelectedDataServers; i++) {
			randomDataServers.add(availableServersAsList.get(randomList.get(i)));
		}

		return randomDataServers;
	}
	
	/**
	 * It returns a list containing unsorted integers between 0 (inclusive) and
	 * n (exclusive). The values do not appear more than one time in the list.
	 * 
	 * @param n
	 *            Upper bound (excluded).
	 * @return A list of n integers.
	 */
	private static List<Integer> random(int n) {

		// n is exclusive, so <=
		if ((n <= 0))
			throw new IllegalArgumentException(
					"ceil value must be positive: n <" + n + ">");

		Random random = new Random();

		List<Integer> samples = new ArrayList<Integer>();
		while (samples.size() < n) {
			int randomValue = random.nextInt(n);
			if (!samples.contains(randomValue)) {
				samples.add(randomValue);
			}
		}
		return samples;
	}

}
