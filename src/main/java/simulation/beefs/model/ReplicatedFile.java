package simulation.beefs.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrick Maia
 */
public class ReplicatedFile {
	
	private static final Logger logger = LoggerFactory
			.getLogger(ReplicatedFile.class);
	
	private final int rLevel;
	private final String fullpath;
	private final DataServer primary;
	private final Set<DataServer> secondaries;
	
	private boolean replicasAreConsistent = true;
	
	/**
	 * Creates a {@link ReplicatedFile} abstraction to model
	 * a distributed store.
	 *  
	 * @param fullpath
	 * @param rLevel The target number of secondary replicas.
	 * @param primary
	 * @param secondaries
	 */
	public ReplicatedFile(String fullpath, int rLevel, DataServer primary,
			Set<DataServer> secondaries) {
		
		if (rLevel < 0) {
			throw new IllegalArgumentException("Replication Level cannot " +
					"be negative");
		}
		
		if (rLevel < secondaries.size()) {
			throw new IllegalArgumentException("We have more secondaries than then" +
					"desired  replication level !");
		}
		
		this.fullpath = fullpath;
		this.rLevel = rLevel;
		this.primary = primary;
		this.secondaries = secondaries;
		
		this.primary.createReplica(fullpath, true);
		for (DataServer sec : secondaries) {
			sec.createReplica(fullpath, false);
		}
	} 
	
	public boolean full() {
		//assuming we cannot f*ck the system and get a collection of replicas
		//large than the rLevel
		return replicationLevel() == this.secondaries.size();
	}
	
	public int replicationLevel() {
		return this.rLevel;
	}
	
	/**
	 * It tries to repair this {@link ReplicatedFile}. It considers candidates
	 * {@link Iterable} is ordered from less to high priority.  
	 * 
	 * @param candidates
	 * @return
	 */
	public boolean repair(Iterable<DataServer> candidates) {
		
		StringBuffer buf = new StringBuffer();
		buf.append("{ ");
		for (DataServer dataServer : secondaries) {
			buf.append(dataServer.toString());
			buf.append(" ");
		}
		buf.append(" }");
		
		logger.info("repair file={} primary={} secs={}", 
				new Object[] {getFullPath(),primary, buf.toString()});
		
		long replicaSize = primary.size(getFullPath());
		
		for (DataServer dataServer : candidates) {
			
			if (full()) {
				break;
			}
			
			if (dataServer.availableSpace() >= replicaSize) {
				
				if (!getSecondaries().contains(dataServer) && 
						!primary.equals(dataServer)) {
					
					dataServer.createReplica(getFullPath(), false);
					dataServer.update(getFullPath(), replicaSize);
					secondaries.add(dataServer);
				}
			}
		}

		return full();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullpath == null) ? 0 : fullpath.hashCode());
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
		ReplicatedFile other = (ReplicatedFile) obj;
		if (fullpath == null) {
			if (other.fullpath != null)
				return false;
		} else if (!fullpath.equals(other.fullpath))
			return false;
		return true;
	}

	public DataServer getPrimary() {
		return primary;
	}
	
	public Set<DataServer> getSecondaries() {
		return new HashSet<DataServer>(secondaries);
	}
	
	public String getFullPath() {
		return fullpath;
	}

	public long getSize() {
		return primary.size(getFullPath());
	}
	
	public void setSize(long size) {
		
		try {
			primary.update(getFullPath(), size);
		} catch (InsufficientSpaceException e) {
			
			Collection<String> purgedReplicas = primary.purge(e.remainder);
			if (purgedReplicas.isEmpty()) {
				throw new InsufficientSpaceException(
						"DataServer purge did not reclaim enough space",
						e.request, e.remainder);
			}
		}
		
		Set<DataServer> secsToRemove = new HashSet<DataServer>();
		for (DataServer sec : secondaries) {
			try {
				sec.update(getFullPath(), size);
			} catch (InsufficientSpaceException e) {
				logger.info("Removing data_server={} from file_group={} " +
						"due to insuf. space. file_size={} available={}",
						new Object[] {sec.toString(), getFullPath(), size, sec.availableSpace()});
				
				sec.delete(getFullPath());
				secsToRemove.add(sec);
			}
		}
		
		secondaries.removeAll(secsToRemove);
	}
	
	public boolean areReplicasConsistent() {
		return replicasAreConsistent;
	}
	
	public void setReplicasAreConsistent(boolean replicasAreConsistent) {
		this.replicasAreConsistent = replicasAreConsistent;
	}
	
}