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
package simulation.beefs.event.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import simulation.beefs.DataServer;
import simulation.beefs.FileSystemClient;
import simulation.beefs.Machine;
import simulation.beefs.MetadataServer;
import simulation.beefs.ReplicationGroup;
import simulation.beefs.placement.CoLocatedWithSecondaryRandomPlacement;
import simulation.beefs.placement.DataPlacementAlgorithm;
import core.EventScheduler;
import core.EventSource;
import core.EventSourceMultiplexer;
import core.Time;
import core.Time.Unit;

/**
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class CloseEventTest {
	
	@Test
	public void closeEventTest() {
		EventSourceMultiplexer eventSource = new EventSourceMultiplexer(new EventSource[0]);
		EventScheduler.setup(new Time(0, Unit.SECONDS), new Time(10, Unit.SECONDS), eventSource);
		
		Machine machine1 = new Machine("machine_1", new Time(15 * 60, Unit.SECONDS));
		Machine machine2 = new Machine("machine_2", new Time(15 * 60, Unit.SECONDS));
		
		DataServer dataServer1 = new DataServer(machine1);
		DataServer dataServer2 = new DataServer(machine2);
		
		machine1.deploy(dataServer1);
		machine2.deploy(dataServer2);
		
		Set<DataServer> dataServers = new HashSet<DataServer>();
		dataServers.add(dataServer1);
		dataServers.add(dataServer2);
		DataPlacementAlgorithm placementAlgorithm = new CoLocatedWithSecondaryRandomPlacement(dataServers);
		
		MetadataServer metadataServer = new MetadataServer(placementAlgorithm, 1, 0, 2, false);
		FileSystemClient client = new FileSystemClient(machine1, metadataServer, false);
		Time time = new Time(5, Unit.SECONDS);
		
		String filePath = "/home/user/file.txt";
		
		ReplicationGroup replicationGroup = metadataServer.openPath(client, filePath);
		replicationGroup.setChanged(true);
		
		CloseEvent close = new CloseEvent(client, time, filePath);
		
		EventScheduler.schedule(close);
		EventScheduler.start();
		
		assertEquals(2, EventScheduler.processCount());
		assertEquals(new Time(7, Unit.SECONDS), EventScheduler.now());
		assertFalse(replicationGroup.isChanged());
	}
	
}
