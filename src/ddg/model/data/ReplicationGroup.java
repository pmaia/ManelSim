/**
 * 
 */
package ddg.model.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ddg.model.Aggregator;
import ddg.model.DDGClient;

/**
 * @author Thiago Emmanuel Pereira da Cunha Silva, thiago.manel@gmail.com
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class ReplicationGroup {

	private static final double NETWORK_BYTES_RW_PER_MILLISECOND = 125; //100 Mb/s == 125 B/ms

	private final String fileName;

	private DataServer primary;
	private Map<String, DataServer> secondaries;

	public ReplicationGroup(String fileName, long initialFileSize,
			final DataServer primaryDataServer,
			final List<DataServer> secondariesReplicas) {

		// FIXME secs should be a set, no duplicates

		if (fileName == null)
			throw new IllegalArgumentException();
		if (primaryDataServer == null)
			throw new IllegalArgumentException();
		if (secondariesReplicas == null)
			throw new IllegalArgumentException();

		this.fileName = fileName;

		this.primary = primaryDataServer;
		this.secondaries = new HashMap<String, DataServer>();
		for (DataServer dataServer : secondariesReplicas) {
			this.secondaries.put(dataServer.getId(), dataServer);
		}

		this.primary.createFile(fileName, initialFileSize);
		for (DataServer secondary : secondariesReplicas) {
			secondary.createFile(fileName, initialFileSize);
		}

	}

	/**
	 * @param dataServer
	 * @param fileName
	 * @param offset
	 * @param length
	 * @param client
	 */
	public void write(String fileName, long offset, long length, DDGClient client) {

		if(!client.getMachine().equals(primary.getMachine()) && !primary.getMachine().isBeingUsed())
			Aggregator.getInstance().reportIdleUtilization(primary.getMachine().getId(), 
					(long)(length / NETWORK_BYTES_RW_PER_MILLISECOND));
		
		dataServerWrite(primary, fileName, offset, length, client);
		
		// considering synchronous replication
		for (DataServer dataserver : secondaries.values()) {
			if(!client.getMachine().equals(dataserver.getMachine()) && !dataserver.getMachine().isBeingUsed())
				Aggregator.getInstance().reportIdleUtilization(dataserver.getMachine().getId(), 
						(long)(length / NETWORK_BYTES_RW_PER_MILLISECOND));
			
			dataServerWrite(dataserver, fileName, offset, length, client);
		}
	}

	private void dataServerWrite(DataServer dataServer, String fileName, long offset,
			long length, DDGClient client) {

		if (!dataServer.containsFile(fileName)) {
			throw new RuntimeException("File " + fileName
					+ " does not exist on data server: " + dataServer.getId());
		}

		dataServer.writeFile(fileName, offset, length, client);
	}


	/**
	 * @param fileName
	 * @param offset
	 * @param length
	 * @param client
	 */
	public void read(String fileName, long offset, long length, DDGClient client) {
		if(!client.getMachine().equals(primary.getMachine()) && !primary.getMachine().isBeingUsed())
			Aggregator.getInstance().
			reportIdleUtilization(primary.getMachine().getId(), (long)(length/NETWORK_BYTES_RW_PER_MILLISECOND));
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