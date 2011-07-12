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
	
	double BYTES_RW_PER_MILLISECOND = 20971.52; //20 MB/s == 20971.52 bytes/ms

	private final String fileName;

	private DataServer primary;
	private Map<String, DataServer> secondaries;

	private Map<Long, Long> nonReplicatedChanges = new HashMap<Long, Long>(); //<offset, length>

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
	
	public boolean isDirty() {
		return !nonReplicatedChanges.isEmpty();
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
					(long)(length / BYTES_RW_PER_MILLISECOND));
		
		nonReplicatedChanges.put(offset, length);
	}
	
	public void updateReplicas() {
		for(DataServer replicaServer : secondaries.values()) {
			if(!primary.getMachine().isBeingUsed()) //FIXME this should happen just once or once per replica server
				for(Long offset : nonReplicatedChanges.keySet())
					Aggregator.getInstance().reportIdleUtilization(primary.getMachine().getId(), 
							(long)(nonReplicatedChanges.get(offset) / BYTES_RW_PER_MILLISECOND));
			
			if(!replicaServer.getMachine().isBeingUsed())
				for(Long offset : nonReplicatedChanges.keySet())
					Aggregator.getInstance().reportIdleUtilization(replicaServer.getMachine().getId(), 
							(long)(nonReplicatedChanges.get(offset) / BYTES_RW_PER_MILLISECOND));
		}
		
		nonReplicatedChanges.clear();
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
				reportIdleUtilization(primary.getMachine().getId(), (long)(length/BYTES_RW_PER_MILLISECOND));
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