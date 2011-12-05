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
import java.util.PriorityQueue;
import java.util.Set;

import ddg.emulator.event.filesystem.FileSystemEventParser;
import ddg.emulator.event.machine.MachineActivityEventParser;
import ddg.kernel.Event;
import ddg.kernel.EventScheduler;
import ddg.model.Aggregator;
import ddg.model.FileSystemClient;
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
			System.exit(1);
		}

		System.out.println(Arrays.toString(args));

		File tracesDir = new File(args[0]);
		if(!tracesDir.exists() || !tracesDir.isDirectory())
			throw new IllegalArgumentException(args[0] + " doesn't exist or is not a directory");

		String placementPoliceName = args[1];
		Long timeBeforeSleep = Long.valueOf(args[2]);
		Integer replicationLevel = Integer.valueOf(args[3]);
		Long timeBeforeUpdateData = Long.valueOf(args[4]);
		Long timeBeforeDeleteData = Long.valueOf(args[5]);
		
		PriorityQueue<Event> eventsGeneratedBySimulationQueue = new PriorityQueue<Event>();

		// building network
		Set<Machine> machines = 
			createMachines(eventsGeneratedBySimulationQueue, tracesDir, timeBeforeSleep);

		Set<DataServer> dataServers = createDataServers(machines);
		
		DataPlacementAlgorithm placement = 
			createPlacementPolice(placementPoliceName, dataServers);

		MetadataServer metadataServer = 
			new MetadataServer(eventsGeneratedBySimulationQueue, placement, replicationLevel, 
					timeBeforeDeleteData, timeBeforeUpdateData);

		Set<FileSystemClient> clients = 
			createClients(eventsGeneratedBySimulationQueue, machines, metadataServer);

		MultipleEventSource multipleEventSource = 
			createMultipleEventParser(clients, machines, tracesDir, eventsGeneratedBySimulationQueue);

		new EventScheduler(multipleEventSource).start();

		System.out.println(Aggregator.getInstance().summarize());
	}

	private static MultipleEventSource createMultipleEventParser(
			Set<FileSystemClient> clients, Set<Machine> machines, File tracesDir,
			PriorityQueue<Event> eventsGeneratedBySimulationQueue) {

		EventSource []  parsers = new EventSource[machines.size() + clients.size()];

		try {
			int parserCount = 0;
			InputStream traceStream;
			for(Machine machine : machines) {
				traceStream = 
					new FileInputStream(new File(tracesDir, "idleness-" + machine.getId()));
				parsers[parserCount++] = new MachineActivityEventParser(machine, traceStream);
			}
			for(FileSystemClient client : clients) {
				traceStream = 
					new FileInputStream(new File(tracesDir, "fs-" + client.getMachine().getId()));
				parsers[parserCount++] = new FileSystemEventParser(client, traceStream);
			}
			
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
		
		return new MultipleEventSource(parsers, eventsGeneratedBySimulationQueue);
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
	 * @param aPlaceForEventsGeneratedBySimulation
	 * @param herald
	 * @param aggregator
	 * @param machines2
	 * @return
	 */
	private static Set<FileSystemClient> createClients(PriorityQueue<Event> aPlaceForEventsGeneratedBySimulation,
			Set<Machine> machines, MetadataServer herald) {

		Set<FileSystemClient> newClients = new HashSet<FileSystemClient>();

		for(Machine machine : machines) {
			newClients.add(new FileSystemClient(aPlaceForEventsGeneratedBySimulation, machine, herald));
		}

		return newClients;
	}

	private static Set<Machine> createMachines(PriorityQueue<Event> aPlaceForEventsGeneratedBySimulation, 
			File tracesDir, long timeBeforeSleep) {
		Set<Machine> machines = new HashSet<Machine>();
		List<String> fsTracesFiles = Arrays.asList(tracesDir.list(fsTracesFilter));
		List<String> idlenessTracesFiles = Arrays.asList(tracesDir.list(idlenessTracesFilter));

		for(String fsTraceFile : fsTracesFiles) {
			String machineName = fsTraceFile.split("-")[1];
			if(idlenessTracesFiles.contains("idleness-" + machineName)) {
				machines.add(new Machine(aPlaceForEventsGeneratedBySimulation, machineName, timeBeforeSleep));
			}
		}

		return machines;
	}

	/**
	 * Create Data Servers.
	 * 
	 * @param aPlaceForEventsGeneratedBySimulation
	 * @param machines
	 * @return
	 */
	private static Set<DataServer> createDataServers(Set<Machine> machines) {

		Set<DataServer> dataServers = new HashSet<DataServer>();

		for (Machine machine : machines) {
			dataServers.add(new DataServer(machine));
		}

		return dataServers;
	}
}
