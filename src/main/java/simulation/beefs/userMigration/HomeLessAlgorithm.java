/**
 * 
 */
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
public class HomeLessAlgorithm implements UserMigrationAlgorithm {
	
	private static final Logger logger = LoggerFactory
			.getLogger(HomeLessAlgorithm.class);

	//FIXME: there is a lot of duplicated code between this class and SweetHome
	private final double swapMachineProb;
	private final Time mSecondsBetweenLogins;

	private final Random random;
	private final List<FileSystemClient> clients;

	private FileSystemClient lastSampledClient;

	private Time lastStamp;

	public HomeLessAlgorithm(double swapMachineProb,
			Time mSecondsBetweenLogins, FileSystemClient firstClient,
			List<FileSystemClient> clients) {

		if (swapMachineProb < 0 || swapMachineProb > 1) {
			throw new IllegalArgumentException();
		}

		this.clients = clients;
		this.lastSampledClient = firstClient;
		
		this.swapMachineProb = swapMachineProb;
		this.mSecondsBetweenLogins = mSecondsBetweenLogins;
		
		this.random = new Random();
	}

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
		FileSystemClient client = (sample <= swapMachineProb) 
				? clients.get(random.nextInt(clients.size())) : lastSampledClient;
				
		logger.info("Time: " + now.toString() + " " +
					"client_host: " + client.getHost().getName());

		return client;
	}
}