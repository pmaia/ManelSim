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
package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import simulation.ConfigurationKeys;


/**
 * 
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

	public static void main(String[] args) throws IOException {
		
		if(args.length < 1) {
			System.out.println("Usage: ManelSim <conf file>");
			System.exit(1);
		}
		
		Properties config = new Properties();
		config.load(new FileInputStream(args[0]));
		
		String tracesDirPath = config.getProperty(ConfigurationKeys.TRACES_DIR);
		File tracesDir = new File(tracesDirPath);
		if(!tracesDir.exists() || !tracesDir.isDirectory())
			throw new IllegalArgumentException(tracesDirPath + " doesn't exist or is not a directory");
		
		
//
//		String placementPoliceName = args[1];
//		Long timeBeforeSleep = Long.valueOf(args[2]);
//		Integer replicationLevel = Integer.valueOf(args[3]);
//		Long timeBeforeUpdateData = Long.valueOf(args[4]);
//		Long timeBeforeDeleteData = Long.valueOf(args[5]);
//		Boolean wakeOnLan = Boolean.valueOf(args[6]);
//		
//		PriorityQueue<Event> eventsGeneratedBySimulationQueue = new PriorityQueue<Event>();
//
//		// building network
//		Set<Machine> machines = 
//			createMachines(eventsGeneratedBySimulationQueue, tracesDir, timeBeforeSleep);
//
//		Set<DataServer> dataServers = createDataServers(machines);
//		
//		DataPlacementAlgorithm placement = 
//			createPlacementPolice(placementPoliceName, dataServers);
//
//		MetadataServer metadataServer = 
//			new MetadataServer(eventsGeneratedBySimulationQueue, placement, replicationLevel, 
//					timeBeforeDeleteData, timeBeforeUpdateData, wakeOnLan);
//
//		Set<FileSystemClient> clients = 
//			createClients(eventsGeneratedBySimulationQueue, machines, metadataServer, wakeOnLan);
//
//		EventSourceMultiplexer multipleEventSource = 
//			createMultipleEventParser(clients, machines, tracesDir, eventsGeneratedBySimulationQueue);
//
//		EventScheduler.setup(emulationStart, emulationEnd, multipleEventSource);
//		EventScheduler.start();
//		
//		System.out.println(Aggregator_.getInstance().summarize());
	}

//	private static EventSourceMultiplexer createMultipleEventParser(Set<FileSystemClient> clients, 
//			Set<Machine> machines, File tracesDir) {
//
//		EventSource []  parsers = new EventSource[machines.size() + clients.size()];
//
//		try {
//			int parserCount = 0;
//			InputStream traceStream;
//			for(Machine machine : machines) {
//				traceStream = 
//					new FileInputStream(new File(tracesDir, "idleness-" + machine.getId()));
//				parsers[parserCount++] = new MachineActivityTraceEventSource(machine, traceStream);
//			}
//			for(FileSystemClient client : clients) {
//				traceStream = 
//					new FileInputStream(new File(tracesDir, "fs-" + client.getMachine().getId()));
//				parsers[parserCount++] = new FileSystemTraceEventSource(client, traceStream);
//			}
//			
//		} catch (FileNotFoundException e) {
//			throw new IllegalStateException(e);
//		}
//		
//		return new EventSourceMultiplexer(parsers);
//	}
//
//	private static DataPlacementAlgorithm createPlacementPolice(String police, Set<DataServer> dataServers) {
//
//		if (police.equals("random")) {
//			return new RandomDataPlacementAlgorithm(dataServers);
//		} else if (police.equals("co-random")) {
//			return new CoLocatedWithSecondaryRandomPlacement(dataServers);
//		} else {
//			throw new RuntimeException("unknown data placement algorithm");
//		}
//
//	}
//
//	private static Set<FileSystemClient> createClients(PriorityQueue<Event> aPlaceForEventsGeneratedBySimulation,
//			Set<Machine> machines, MetadataServer metadataServer, boolean wakeOnLan) {
//
//		Set<FileSystemClient> newClients = new HashSet<FileSystemClient>();
//
//		for(Machine machine : machines) {
//			newClients.add(new FileSystemClient(aPlaceForEventsGeneratedBySimulation, machine, metadataServer, wakeOnLan));
//		}
//
//		return newClients;
//	}
//
//	private static Set<Machine> createMachines(File tracesDir, long timeBeforeSleep) {
//		Set<Machine> machines = new HashSet<Machine>();
//		List<String> fsTracesFiles = Arrays.asList(tracesDir.list(fsTracesFilter));
//		List<String> idlenessTracesFiles = Arrays.asList(tracesDir.list(idlenessTracesFilter));
//
//		for(String fsTraceFile : fsTracesFiles) {
//			String machineName = fsTraceFile.split("-")[1];
//			if(idlenessTracesFiles.contains("idleness-" + machineName)) {
//				machines.add(new Machine(machineName, new Time(timeBeforeSleep, Unit.SECONDS)));
//			}
//		}
//
//		return machines;
//	}
//
//	private static Set<DataServer> createDataServers(Set<Machine> machines) {
//
//		Set<DataServer> dataServers = new HashSet<DataServer>();
//
//		for (Machine machine : machines) {
//			dataServers.add(new DataServer(machine));
//		}
//
//		return dataServers;
//	}
}
