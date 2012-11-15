package simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import simulation.beefs.event.filesystem.MobileClientFSTraceEventSource;
import simulation.beefs.event.filesystem.FileSystemTraceEventSource;
import simulation.beefs.model.DataServer;
import simulation.beefs.model.FileSystemClient;
import simulation.beefs.model.Machine;
import simulation.beefs.model.MetadataServer;
import simulation.beefs.model.ReplicatedFile;
import simulation.beefs.placement.CoLocatedWithSecondaryRandomPlacement;
import simulation.beefs.placement.DataPlacementUtil;
import simulation.beefs.placement.RandomDataPlacementAlgorithm;
import simulation.beefs.userMigration.HomeLessAlgorithm;
import simulation.beefs.userMigration.SweetHomeAlgorithm;
import simulation.beefs.userMigration.UserMigrationAlgorithm;
import simulation.util.FileSizeDistribution;
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

	private static final Logger logger = LoggerFactory.getLogger(BeefsLocalitySimulationInitializer.class);
	
	private static long diskSize = 1024 * 1024 * 1024 * 1L;// 1 GiBytes

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
		Set<DataServer> dataServers = createDataServers(machines);
		
		//populate data server
		Integer replicationLevel = Integer.valueOf(config.getProperty(BeefsLocalitySimulationConstants.REPLICATION_LEVEL));
		Set<ReplicatedFile> namespace = populate(5, replicationLevel, dataServers);

		//create metadata server
		String placementPoliceName = config.getProperty(BeefsLocalitySimulationConstants.PLACEMENT_POLICE);
		Time timeToCoherence = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.TIME_TO_COHERENCE)), 
						Unit.SECONDS);
		Time timeToDelete = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.TIME_TO_DELETE_REPLICAS)), 
						Unit.SECONDS);
		
		MetadataServer metadataServer = 
				new MetadataServer(namespace, dataServers, placementPoliceName,
						replicationLevel, timeToCoherence, timeToDelete); 

		// create clients
		Boolean wakeOnLan = Boolean.valueOf(config.getProperty(BeefsLocalitySimulationConstants.WAKE_ON_LAN));
		Set<FileSystemClient> clients = createClients(machines, metadataServer, wakeOnLan);
		
		//create user migration algorithm
		String userMigrationAlgorithmName = 
				config.getProperty(BeefsLocalitySimulationConstants.USER_MIGRATION_ALGORITHM);
		double userMigrationProb = Double.valueOf(
				config.getProperty(BeefsLocalitySimulationConstants.USER_MIGRATION_PROB));
		Time inactivityDelay = 
				new Time(Long.valueOf(config.getProperty(BeefsLocalitySimulationConstants.USER_INACTIVITY_DELAY)),
						Unit.SECONDS);
		
		List<FileSystemClient> otherClients = new LinkedList<FileSystemClient>(clients);
		FileSystemClient firstClient = otherClients.remove(0);
		
		UserMigrationAlgorithm loginAlgorithm = createLoginAlgorithm(
				userMigrationAlgorithmName,
				inactivityDelay, userMigrationProb,
				firstClient, otherClients);
		
		// setup context
		EventSourceMultiplexer eventSourceMultiplexer =	
				createEventSourceMultiplexer(firstClient, loginAlgorithm, fstraceFile);
		
		Context context = new Context(eventSourceMultiplexer);
		context.add(BeefsLocalitySimulationConstants.MACHINES, machines);
		context.add(BeefsLocalitySimulationConstants.DATA_SERVERS, dataServers);
		context.add(BeefsLocalitySimulationConstants.METADATA_SERVER, metadataServer);
		context.add(BeefsLocalitySimulationConstants.CLIENTS, clients);

		for (DataServer server : dataServers) {
			logger.info("ds={} available={} total={}",	
					new Object[] {server, server.availableSpace(), server.totalSpace()});
		}
		
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
	
	//FIXME: we need to relate the brand new files created during the
	//simulation and this pre-population. What we want to tell are
	//the fractions: disk size, available size, created_files
	/**
	 * @param numFullDSs
	 * @param rlevel
	 * @param dataServers
	 * @return
	 * 	The generate namespace.
	 */
	private Set<ReplicatedFile> populate (int numFullDSs, int rlevel, 
			Set<DataServer> dataServers) {
		
		Set<ReplicatedFile> namespace = new HashSet<ReplicatedFile>();
		
		FileSizeDistribution fileSizeDistribution =
				new FileSizeDistribution(8.46, 2.38, diskSize);
		
		Set<DataServer> halfFilled = new HashSet<DataServer>();
		
		//phase 1. we need to fill, at least, 0.5 of their disk space
		while (	halfFilled.size() < dataServers.size()) {
			
			double newFileSize = fileSizeDistribution.nextSampleSize();
			Set<DataServer> candidates = filter(newFileSize, dataServers);
			
			if (!candidates.isEmpty()) {
				String randomFullPath = UUID.randomUUID().toString();
				ReplicatedFile replicatedFile = 
						new RandomDataPlacementAlgorithm(candidates).
						createFile(null, randomFullPath, rlevel);
				replicatedFile.setSize((long) newFileSize);
				
				redistributed(replicatedFile.getPrimary(), halfFilled);
				for (DataServer sec : replicatedFile.getSecondaries()) {
					redistributed(sec, halfFilled);
				}
				
				namespace.add(replicatedFile);
			}
		}
		
		//phase 2. we need to full N = numFullDSs
		Set<DataServer> fullDSsCandidates = 
				DataPlacementUtil.chooseRandomDataServers(dataServers, numFullDSs);
		
		for (DataServer toFill : fullDSsCandidates) {
			namespace.addAll(fill(0.99, toFill, dataServers, fileSizeDistribution, rlevel));
		}
		
		return namespace;
	}
	
	private Set<ReplicatedFile> fill(double targetFullNess, DataServer targetPrimary, 
			Set<DataServer> secCandidates, FileSizeDistribution sizeDistribution, 
			int rlevel) {
		
		Set<ReplicatedFile> namespace = new HashSet<ReplicatedFile>();
		
		while (  (1 - ( ((float) targetPrimary.availableSpace()) / ((float)targetPrimary.totalSpace()) )) 
				< targetFullNess) {
			
			double newFileSize = 
					Math.min(targetPrimary.availableSpace(),
							sizeDistribution.nextSampleSize());
			
			Set<DataServer> candidates = filter(newFileSize, secCandidates);
			
			String randomFullPath = UUID.randomUUID().toString();
			ReplicatedFile replicatedFile = 
					new CoLocatedWithSecondaryRandomPlacement(candidates).
					createFile(targetPrimary, randomFullPath, rlevel);
		
			replicatedFile.setSize((long) newFileSize);
			namespace.add(replicatedFile);
		}
		
		return namespace;
	}
	
	private Set<DataServer> filter(double minAvailableSpace, Set<DataServer> servers) {
		
		Set<DataServer> response = new HashSet<DataServer>();
		for (DataServer dataServer : servers) {
			
			if (dataServer.availableSpace() >= minAvailableSpace) {
				response.add(dataServer);
			}
		}
		return response;
	}
	
	private void redistributed(DataServer target, Set<DataServer> halfFilled) {
		
		if (((float)target.availableSpace() / (float)target.totalSpace()) <= 0.5) {
			halfFilled.add(target);
		}
	}

	private EventSourceMultiplexer createEventSourceMultiplexer(
			FileSystemClient firsClient, 
			UserMigrationAlgorithm loginAlgorithm, File fstraceFile) {

		try {
			InputStream traceStream = new FileInputStream(fstraceFile);
			FileSystemTraceEventSource fileSystemTraceEventSource =
					new FileSystemTraceEventSource(firsClient, traceStream);
			
			MobileClientFSTraceEventSource mobileSource = 
					new MobileClientFSTraceEventSource(
							loginAlgorithm, fileSystemTraceEventSource);
			
			EventSource [] parsers = new EventSource[] {mobileSource};
			
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
			dataServers.add(new DataServer(machine, diskSize));
		}
	
		return dataServers;
	}

}
