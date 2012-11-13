package simulation.beefs.userMigration;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.beefs.model.FileSystemClient;
import core.Time;

/**
 * @author manel
 */
public class SweetHomeAlgorithm implements UserMigrationAlgorithm {

	private static final Logger logger = LoggerFactory
			.getLogger(HomeLessAlgorithm.class);
	
	private final Random random;
	
	private final FileSystemClient sweetHomeClient;
	private final List<FileSystemClient> othersClients;

	private final double migrationProb;
	private final Time mSecondsBetweenLogins;

	private Time lastStamp;

	private FileSystemClient lastSampledClient;

	public SweetHomeAlgorithm(double swapMachineProb,
			Time mSecondsBetweenLogins, FileSystemClient sweetHomeClient,
			List<FileSystemClient> othersClients) {

		if (swapMachineProb < 0 || swapMachineProb >= 1) {
			throw new IllegalArgumentException();
		}

		if (othersClients.contains(sweetHomeClient)) {
			throw new IllegalArgumentException();
		}

		this.migrationProb = swapMachineProb;
		this.mSecondsBetweenLogins = mSecondsBetweenLogins;
		
		this.othersClients = othersClients;
		this.sweetHomeClient = sweetHomeClient;
		this.lastSampledClient = sweetHomeClient;
		
		this.random = new Random();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileSystemClient baseClient(Time now) {

		if (lastStamp == null) {
			lastStamp = now;
		}
		
		lastSampledClient = (now.minus(lastStamp).isEarlierThan(mSecondsBetweenLogins)) 
				? lastSampledClient : pickAClient(now);
		lastStamp = now;

		return lastSampledClient;
	}

	private FileSystemClient pickAClient(Time now) {

		double sample = random.nextDouble();
		FileSystemClient client = (sample <= migrationProb) 
				? othersClients.get(random.nextInt(othersClients.size())) : sweetHomeClient;
		
		logger.info("Time: " + now.toString() + " " +
					"client_host: " + client.getHost().getName());
		
		return client;
	}

}