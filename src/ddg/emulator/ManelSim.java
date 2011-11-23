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
package ddg.emulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ddg.emulator.event.filesystem.FileSystemEventParser;
import ddg.emulator.event.machine.UserIdlenessEventParser;
import ddg.kernel.EventScheduler;
import ddg.model.Aggregator;
import ddg.model.DDGClient;
import ddg.model.Machine;
import ddg.model.MetadataServer;
import ddg.model.data.DataServer;
import ddg.model.placement.CoLocatedWithSecondaryRandomPlacement;
import ddg.model.placement.DataPlacementAlgorithm;
import ddg.model.placement.RandomDataPlacementAlgorithm;

/**
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 */
public class ManelSim {

	private static final FilenameFilter fsTracesFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith("fs-");
		}
	};

	private static final FilenameFilter idlenessTracesFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith("idleness-");
		}
	};

	/**
	 * @param args
	 * [0] (traces dir) - the traces in the directory must have the name &lt;trace type&gt;-&lt;machine name&gt;, 
	 * where &lt;trace type&gt; could be either fs or idleness. All traces must come in pairs of fs and idleness. Single traces will be ignored.
	 * Ex.: fs-cherne, idleness-cherne
	 * @param args
	 * [1] (data placement police) - random, co-random or co-balance
	 * @param args 
	 * [2] time before sleep (secs)
	 * @param args 
	 * [3] replication level
	 * @param args
	 * [4] time before update replicas' data (secs)
	 * @param args
	 * [5] time before delete replicas' data (secs)
	 */
	public static void main(String[] args) throws IOException {
		
		if(args.length != 6) {
			System.out.println("Usage: ManelSim <traces dir> <data placement policement>" +
					" <time before sleep> <replication level> <time before update replicas>" +
					" <time before delete replicas>");
		}

		System.out.println(Arrays.toString(args));

		final EventScheduler scheduler = new EventScheduler();

		File tracesDir = new File(args[0]);
		if(!tracesDir.exists() || !tracesDir.isDirectory())
			throw new IllegalArgumentException(args[0] + " doesn't exist or is not a directory");

		String placementPoliceName = args[1];
		Long timeBeforeSleep = Long.valueOf(args[2]);
		Integer replicationLevel = Integer.valueOf(args[3]);
		Long timeBeforeUpdateData = Long.valueOf(args[4]);
		Long timeBeforeDeleteData = Long.valueOf(args[5]);

		// building network
		Set<Machine> machines = 
			createMachines(scheduler, tracesDir, timeBeforeSleep);

		Set<DataServer> dataServers = 
			createDataServers(scheduler, machines);
		
		DataPlacementAlgorithm placement = 
			createPlacementPolice(placementPoliceName, dataServers);

		MetadataServer metadataServer = 
			new MetadataServer(scheduler, placement, replicationLevel, 
					timeBeforeDeleteData, timeBeforeUpdateData);

		Set<DDGClient> clients = 
			createClients(scheduler, machines, metadataServer);

		MultipleEventSource multipleEventSource = 
			createMultipleEventParser(clients, machines, tracesDir);

		EventInjector eventInjector = 
			new EventInjector(scheduler, multipleEventSource);
		
		scheduler.registerObserver(eventInjector);
		
		eventInjector.injectNext();
		scheduler.start();

		System.out.println(Aggregator.getInstance().summarize());
	}

	private static MultipleEventSource createMultipleEventParser(
			Set<DDGClient> clients, Set<Machine> machines, File tracesDir) {

		EventSource []  parsers = new EventSource[machines.size() + clients.size()];

		try {
			int parserCount = 0;
			InputStream traceStream;
			for(Machine machine : machines) {
				traceStream = 
					new FileInputStream(new File(tracesDir, "idleness-" + machine.getId()));
				parsers[parserCount++] = new UserIdlenessEventParser(machine, traceStream);
			}
			for(DDGClient client : clients) {
				traceStream = 
					new FileInputStream(new File(tracesDir, "fs-" + client.getMachine().getId()));
				parsers[parserCount++] = new FileSystemEventParser(client, traceStream);
			}
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}

		return new MultipleEventSource(parsers);
	}

	private static DataPlacementAlgorithm createPlacementPolice(String police, Set<DataServer> dataServers) {

		if (police.equals("random")) {
			return new RandomDataPlacementAlgorithm(dataServers);
		} else if (police.equals("co-random")) {
			return new CoLocatedWithSecondaryRandomPlacement(dataServers);
		} else {
			throw new RuntimeException("unknown data placement algorithm");
		}

	}

	/**
	 * It create all clients.
	 * 
	 * @param scheduler
	 * @param herald
	 * @param aggregator
	 * @param machines2
	 * @return
	 */
	private static Set<DDGClient> createClients(EventScheduler scheduler, Set<Machine> machines, MetadataServer herald) {

		Set<DDGClient> newClients = new HashSet<DDGClient>();

		for(Machine machine : machines) {
			newClients.add(new DDGClient(scheduler, machine, herald));
		}

		return newClients;
	}

	private static Set<Machine> createMachines(EventScheduler scheduler, File tracesDir, long timeBeforeSleep) {
		Set<Machine> machines = new HashSet<Machine>();
		List<String> fsTracesFiles = Arrays.asList(tracesDir.list(fsTracesFilter));
		List<String> idlenessTracesFiles = Arrays.asList(tracesDir.list(idlenessTracesFilter));

		for(String fsTraceFile : fsTracesFiles) {
			String machineName = fsTraceFile.split("-")[1];
			if(idlenessTracesFiles.contains("idleness-" + machineName)) {
				machines.add(new Machine(scheduler, machineName, timeBeforeSleep));
			}
		}

		return machines;
	}

	/**
	 * Create Data Servers.
	 * 
	 * @param scheduler
	 * @param machines
	 * @return
	 */
	private static Set<DataServer> createDataServers(EventScheduler scheduler, Set<Machine> machines) {

		Set<DataServer> dataServers = new HashSet<DataServer>();

		for (Machine machine : machines) {
			dataServers.add(new DataServer(scheduler, machine));
		}

		return dataServers;
	}
}
