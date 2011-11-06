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

import ddg.kernel.EventScheduler;
import ddg.model.Aggregator;
import ddg.model.DDGClient;
import ddg.model.Machine;
import ddg.model.MetadataServer;
import ddg.model.data.DataServer;
import ddg.model.placement.CoLocatedWithSecondariesLoadBalance;
import ddg.model.placement.CoLocatedWithSecondaryRandomPlacement;
import ddg.model.placement.DataPlacementAlgorithm;
import ddg.model.placement.RandomDataPlacementAlgorithm;

/**
 * TODO make doc
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
	 */
	public static void main(String[] args) throws IOException {

		System.out.println(Arrays.toString(args));

		final EventScheduler scheduler = new EventScheduler();

		File tracesDir = new File(args[0]);
		if(!tracesDir.exists() || !tracesDir.isDirectory())
			throw new IllegalArgumentException(args[0] + " doesn't exist or is not a directory");

		String placementPoliceName = args[1];
		Long timeBeforeSleep = Long.valueOf(args[2]);
		Integer replicationLevel = Integer.valueOf(args[3]);

		// 1 GiBytes
		long one_GB = 1024 * 1024 * 1024 * 1L;
		
		// building network
		Set<Machine> machines = 
			createMachines(scheduler, tracesDir, timeBeforeSleep);

		Set<DataServer> dataServers = 
			createDataServers(scheduler, one_GB, machines);
		
		DataPlacementAlgorithm placement = 
			createPlacementPolice(placementPoliceName, dataServers);

		MetadataServer metadataServer = 
			new MetadataServer(placement, replicationLevel);

		Set<DDGClient> clients = 
			createClients(scheduler, machines, metadataServer);

		MultipleEventParser multipleEventParser = 
			createMultipleEventParser(clients, machines, tracesDir);

		EmulatorControl control = 
			EmulatorControl.build(scheduler, multipleEventParser, metadataServer);

		control.scheduleNext();
		scheduler.start();

		System.out.println(Aggregator.getInstance().summarize());
	}

	private static MultipleEventParser createMultipleEventParser(
			Set<DDGClient> clients, Set<Machine> machines, File tracesDir) {

		EventParser []  parsers = new EventParser[machines.size() + clients.size()];

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
				parsers[parserCount++] = new FileSystemEventParser(traceStream, client);
			}
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}

		return new MultipleEventParser(parsers);
	}

	private static DataPlacementAlgorithm createPlacementPolice(String police, Set<DataServer> dataServers) {

		if (police.equals("random")) {
			return new RandomDataPlacementAlgorithm(dataServers);
		} else if (police.equals("co-random")) {
			return new CoLocatedWithSecondaryRandomPlacement(dataServers);
		} else if (police.equals("co-balance")) {
			return new CoLocatedWithSecondariesLoadBalance(dataServers);
		}

		return null;
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
	 * @param numberOfDataServers
	 * @param diskSize
	 * @param machines
	 * @return
	 */
	private static Set<DataServer> createDataServers(EventScheduler scheduler, double diskSize, Set<Machine> machines) {

		Set<DataServer> dataServers = new HashSet<DataServer>();

		for (Machine machine : machines) {
			dataServers.add(new DataServer(scheduler, machine, diskSize));
		}

		return dataServers;
	}
}
