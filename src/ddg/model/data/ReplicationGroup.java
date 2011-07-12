/**
 * 
 */
package ddg.model.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ddg.emulator.EmulatorControl;
import ddg.emulator.events.metadataServerEvents.MigrateEvent;
import ddg.kernel.JEEventScheduler;
import ddg.kernel.JETime;
import ddg.model.Aggregator;
import ddg.model.Aggregator.DataOperation;
import ddg.model.DDGClient;
import ddg.model.Machine;

/**
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class ReplicationGroup implements Comparable<ReplicationGroup> {

	private JETime timeStamp;
	private JETime creationTime;

	private final String fileName;

	private int replicationLevel;
	private final JEEventScheduler theUniqueEventScheduler;
	private final EmulatorControl emulatorControl;

	private DataServer primary;
	private Map<String, DataServer> secondaries;

	private boolean processingMigration = false;
	private final JETime replicationDelay;

	/**
	 * Default constructor using fields.
	 * 
	 * @param fileName
	 * @param initialFileSize
	 * @param replicationLevel
	 *            TODO
	 * @param primaryDataServer
	 * @param secondariesReplicas
	 */
	public ReplicationGroup(String fileName, long initialFileSize,
			int replicationLevel, final DataServer primaryDataServer,
			final List<DataServer> secondariesReplicas) {

		// FIXME secs should be a set, no duplicates

		if (fileName == null)
			throw new IllegalArgumentException();
		if (primaryDataServer == null)
			throw new IllegalArgumentException();
		if (secondariesReplicas == null)
			throw new IllegalArgumentException();

		this.fileName = fileName;
		this.replicationLevel = replicationLevel;

		this.emulatorControl = EmulatorControl.getInstance();
		this.theUniqueEventScheduler = emulatorControl
				.getTheUniqueEventScheduler();
		this.replicationDelay = new JETime(
				emulatorControl.replicationDelayMillis);

		this.primary = primaryDataServer;
		this.secondaries = new HashMap<String, DataServer>();
		for (DataServer dataServer : secondariesReplicas) {
			this.secondaries.put(dataServer.getId(), dataServer);
		}

		this.primary.createFile(fileName, initialFileSize);
		for (DataServer secondary : secondariesReplicas) {
			secondary.createFile(fileName, initialFileSize);
		}

		this.creationTime = theUniqueEventScheduler.now();
	}

	/**
	 * @param newDataServer
	 */
	public void migrate(DataServer newDataServer) {

		if (hasReplica(newDataServer))
			throw new RuntimeException();

		String id = secondaries.keySet().iterator().next();
		DataServer dataServer = secondaries.remove(id);

		dataServer.removeFileReplica(getFileName());

		long fileSize = primary.getFileSize(getFileName());
		newDataServer.createFile(fileName, fileSize);
		secondaries.put(newDataServer.getId(), newDataServer);

		processingMigration = false;
	}

	private void promoteSecondary(DataServer secondary) {

		Aggregator.getInstance().report(
				"promoting " + getTime() + " "
						+ emulatorControl.getTheUniqueEventScheduler().now()
						+ " " + fileName);

		if (secondaries.remove(secondary.getId()) == null) {// double checking
			throw new RuntimeException();
		}
		// promoting to primary
		secondaries.put(primary.getId(), primary);
		primary = secondary;
	}

	// 1Mbit/second
	private final long sustainedThroughtput_bits_per_sec = 1000000;

	private void scheduleMigration(DataServer dstDataServer) {

		long delta = (long) (2 + Math
				.ceil((primary.getFileSize(getFileName()) * 8)
						/ sustainedThroughtput_bits_per_sec)); // 2 millisecond
																// by control
																// messages
		JETime time = emulatorControl.getTheUniqueEventScheduler().now()
				.plus(new JETime(delta * 1000));
		MigrateEvent migrateEvent = new MigrateEvent(getFileName(),
				dstDataServer, emulatorControl.getMetadataServer(), time);
		emulatorControl.scheduleNext(migrateEvent);
	}

	private boolean hasReplica(DataServer dataServer) {
		return isSecondary(dataServer) || isPrimary(dataServer);
	}

	private boolean isSecondary(DataServer dataServer) {
		return secondaries.containsKey(dataServer.getId());
	}

	private boolean isPrimary(DataServer dataServer) {
		return dataServer.equals(primary);
	}

	/**
	 * @param dataServer
	 * @param fileName
	 * @param offset
	 * @param length
	 * @param client
	 */
	public void writeFile(String fileName, long offset, long length,
			DDGClient client) {

		setTouchTimeStamp(EmulatorControl.getInstance()
				.getTheUniqueEventScheduler().now());

		checkDataMigrationAndPromotion(client);
		reportDataOperation(client.getMachine(), primary, DataOperation.WRITE,
				length);

		writeFile(primary, fileName, offset, length, client);
		for (DataServer dataserver : secondaries.values()) {
			writeFile(dataserver, fileName, offset, length, client);
		}
	}

	private void writeFile(DataServer dataServer, String fileName, long offset,
			long length, DDGClient client) {

		if (!dataServer.containsFile(fileName)) {
			throw new RuntimeException("File " + fileName
					+ " does not exist on data server: " + dataServer.getId());
		}

		dataServer.writeFile(fileName, offset, length, client);
	}

	private void checkDataMigrationAndPromotion(DDGClient client) {

		DataServer clientDS = client.getMachine().getDeployedDataServers()
				.iterator().next();

		if (!hasReplica(clientDS) && emulatorControl.datamigration) {
			if (!processingMigration) {
				processingMigration = true;
				scheduleMigration(client.getMachine().getDeployedDataServers()
						.iterator().next());
			}
		} else { // has a replica but is a secondary, promote
			if (isSecondary(clientDS) && isUptoDate()) {
				promoteSecondary(clientDS);
			}
		}
	}

	private boolean isUptoDate() {
		return getTime().plus(replicationDelay).isEarlierThan(
				emulatorControl.getTheUniqueEventScheduler().now());
	}

	/**
	 * @param fileName
	 * @param offset
	 * @param length
	 * @param client
	 */
	public void read(String fileName, long offset, long length, DDGClient client) {
		checkDataMigrationAndPromotion(client);
		reportDataOperation(client.getMachine(), primary, DataOperation.READ,
				length);
	}

	private void reportDataOperation(Machine machine, DataServer dataServer,
			DataOperation dataOperation, long length) {
		Aggregator.getInstance().reportDataOperation(fileName, dataOperation,
				length, machine.isDeployed(dataServer), machine.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(ReplicationGroup group) {
		return getTime().compareTo(group.getTime());
	}

	private JETime getTime() {
		return (getTouchTimeStamp() != null) ? getTouchTimeStamp()
				: creationTime;
	}

	private void setTouchTimeStamp(JETime touchTimeStamp) {
		this.timeStamp = touchTimeStamp;
	}

	private JETime getTouchTimeStamp() {
		return timeStamp;
	}

	private String getDsString(DataServer dataServer, String fileName) {
		return dataServer.getId();
	}

	/**
	 * @return the file
	 */
	public String getFileName() {
		return fileName;
	}

	@Override
	public String toString() {

		String header = getFileName() + "#"
				+ getDsString(primary, getFileName());

		for (DataServer sec : secondaries.values()) {
			header += "#" + getDsString(sec, getFileName());
		}

		return header;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReplicationGroup other = (ReplicationGroup) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		return true;
	}

}