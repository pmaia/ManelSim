package simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import simulation.beefs.event.filesystem.FileSystemTraceEventSource;
import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.Machine;
import simulation.beefs.model.MetadataServer;
import simulation.beefs.userMigration.HomeLessAlgorithm;
import simulation.beefs.userMigration.SweetHomeAlgorithm;
import simulation.beefs.userMigration.UserMigrationAlgorithm;
import core.Context;
import core.EventSource;
import core.EventSourceMultiplexer;
import core.Initializer;
import core.Time;
import core.Time.Unit;

/**
 * 
 * @author manel
 */
public class BeefsLocalitySimulationInitializer implements Initializer {
	
	@Override
	public Context initialize(Properties config) {
		
		String fstracePath = config.getProperty(BeefsLocalitySimulationConstants.FS_TRACE_FILE);
		File fstraceFile = new File(fstracePath);
		if(!fstraceFile.exists() || !fstraceFile.isFile())
			throw new IllegalArgumentException(fstracePath + " doesn't exist or is not a file");

		// create machines
		Time toSleepTimeout = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.TO_SLEEP_TIMEOUT)), 
						Unit.SECONDS);
		Time transitionDuration = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.TRANSITION_DURATION)), 
						Unit.MILLISECONDS);
		int numMachines = Integer.valueOf(config.getProperty(BeefsLocalitySimulationConstants.NUM_MACHINES));
		
		Set<Machine> machines = createMachines(numMachines, toSleepTimeout, transitionDuration);

		// create data servers
		//FIXME: fill data servers
		Set<DataServer> dataServers = createDataServers(machines);

		//create metadata server
		String placementPoliceName = config.getProperty(BeefsLocalitySimulationConstants.PLACEMENT_POLICE);
		Integer replicationLevel = Integer.valueOf(config.getProperty(BeefsLocalitySimulationConstants.REPLICATION_LEVEL));
		Time timeToCoherence = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.TIME_TO_COHERENCE)), 
						Unit.SECONDS);
		Time timeToDelete = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.TIME_TO_DELETE_REPLICAS)), 
						Unit.SECONDS);
		MetadataServer metadataServer = 
				new MetadataServer(dataServers, placementPoliceName, replicationLevel, timeToCoherence, timeToDelete); 

		// create clients
		Boolean wakeOnLan = Boolean.valueOf(config.getProperty(BeefsLocalitySimulationConstants.WAKE_ON_LAN));
		Set<FileSystemClient> clients = createClients(machines, metadataServer, wakeOnLan);
		
		//create user migration algorithm
		String userMigrationAlgorithmName = 
				config.getProperty(BeefsLocalitySimulationConstants.USER_MIGRATION_ALGORITHM);
		double userMigrationProb = Double.valueOf(
				config.getProperty(BeefsLocalitySimulationConstants.USER_MIGRATION_PROB));
		Time userMigrationDelay = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.USER_MIGRATION_DELAY)),
						Unit.MILLISECONDS);
		
		List<FileSystemClient> otherClients = new LinkedList<FileSystemClient>(clients);
		FileSystemClient firstClient = otherClients.remove(0);
		
		UserMigrationAlgorithm loginAlgorithm = createLoginAlgorithm(
				userMigrationAlgorithmName,
				userMigrationDelay, userMigrationProb,
				firstClient, otherClients);
		
		// setup context
		EventSourceMultiplexer eventSourceMultiplexer =	
				createEventSourceMultiplexer(firstClient, loginAlgorithm, fstraceFile);
		
		Context context = new Context(eventSourceMultiplexer);
		context.add(BeefsLocalitySimulationConstants.MACHINES, machines);
		context.add(BeefsLocalitySimulationConstants.DATA_SERVERS, dataServers);
		context.add(BeefsLocalitySimulationConstants.METADATA_SERVER, metadataServer);
		context.add(BeefsLocalitySimulationConstants.CLIENTS, clients);

		return context;
	}
	
	private UserMigrationAlgorithm createLoginAlgorithm(String type,
				Time migrationDelay, double migrationProb, 
				FileSystemClient firstClient, List<FileSystemClient> otherClients) {
		
		if("sweet_home".equals(type)) {
			return new SweetHomeAlgorithm(migrationProb, migrationDelay, firstClient, otherClients);
		} else if("homeless".equals(type)) {
			return new HomeLessAlgorithm(migrationProb, migrationDelay, firstClient, otherClients);
		} else {
			throw new IllegalArgumentException(type + " is not a valid DataPlacementAlgorithm type.");
		}
	}

	private EventSourceMultiplexer createEventSourceMultiplexer(
			FileSystemClient firsClient, 
			UserMigrationAlgorithm loginAlgorithm, File fstraceFile) {

		try {
			InputStream traceStream = new FileInputStream(fstraceFile);
			EventSource [] parsers = new EventSource[] { 
					new FileSystemTraceEventSource(firsClient, traceStream) };
			
			return new EventSourceMultiplexer(parsers);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private Set<Machine> createMachines(int numMachines, Time toSleepTimeout, Time transitionDuration) {
		
		Set<Machine> machines = new HashSet<Machine>();
		
		for (int i = 0; i < numMachines; i++) {
			String machineName = "machine-" + i;
			machines.add(new Machine(machineName, toSleepTimeout, transitionDuration));
		}
	
		return machines;
	}
	
	private Set<FileSystemClient> createClients(Set<Machine> machines, 
			MetadataServer metadataServer, boolean wakeOnLan) {
	
		Set<FileSystemClient> newClients = new HashSet<FileSystemClient>();
	
		for(Machine machine : machines) {
			newClients.add(new FileSystemClient(machine, metadataServer, wakeOnLan));
		}
	
		return newClients;
	}
	
	private Set<DataServer> createDataServers(Set<Machine> machines) {
		
		Set<DataServer> dataServers = new HashSet<DataServer>();
	
		for (Machine machine : machines) {
			dataServers.add(new DataServer(machine));
		}
	
		return dataServers;
	}

}