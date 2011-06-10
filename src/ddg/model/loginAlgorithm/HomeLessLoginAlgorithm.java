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
package ddg.model.loginAlgorithm;

import java.util.List;
import java.util.Random;

import ddg.model.Aggregator;
import ddg.model.DDGClient;

/**
 * TODO make doc
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public class HomeLessLoginAlgorithm implements LoginAlgorithm {

	private final double swapMachineProb;
	private final long mSecondsBetweenLogins;

	private final Random random;
	private final List<DDGClient> clients;

	private DDGClient lastSampledClient;
	
	private long lastStamp;

	/**
	 * @param swapMachineProb
	 * @param mSecondsBetweenLogins
	 * @param firstClient
	 * @param clients
	 */
	public HomeLessLoginAlgorithm(double swapMachineProb, long mSecondsBetweenLogins, DDGClient firstClient, List<DDGClient> clients) {

		if (swapMachineProb < 0 || swapMachineProb > 1) {
			throw new IllegalArgumentException();
		}

		this.clients = clients;
		this.lastSampledClient = firstClient;
		this.swapMachineProb = swapMachineProb;
		this.mSecondsBetweenLogins = mSecondsBetweenLogins;
		this.random = new Random();
		
		this.lastStamp = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public DDGClient sampleClient(long now) {
		
		if (lastStamp == -1) {
			lastStamp = now;
		}
		
		lastSampledClient = ( ( (now - lastStamp) < mSecondsBetweenLogins)) ? lastSampledClient : pickAClient(now);
		lastStamp = now;
		
		return lastSampledClient;
	}

	private DDGClient pickAClient(long now) {
		double sample = random.nextDouble();
		DDGClient client = (sample <= swapMachineProb) ? clients.get(random.nextInt(clients.size())) : lastSampledClient;
		Aggregator.getInstance().reportlogin(client, now);
		return client;
	}

}