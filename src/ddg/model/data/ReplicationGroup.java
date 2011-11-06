/**
 * 
 */
package ddg.model.data;

import java.util.Set;

public class ReplicationGroup {

	private final String fileName;
	private final DataServer primary;
	private final Set<DataServer> secondaries;

	public ReplicationGroup(String fileName, DataServer primary, Set<DataServer> secondaries) {

		if (fileName == null)
			throw new IllegalArgumentException();
		if (primary == null)
			throw new IllegalArgumentException();
		if (secondaries == null)
			throw new IllegalArgumentException();

		this.fileName = fileName;
		this.primary = primary;
		this.secondaries = secondaries;

	}

	public String getFileName() {
		return fileName;
	}

	public DataServer getPrimary() {
		return primary;
	}

	public Set<DataServer> getSecondaries() {
		return secondaries;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((primary == null) ? 0 : primary.hashCode());
		result = prime * result
				+ ((secondaries == null) ? 0 : secondaries.hashCode());
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
		if (primary == null) {
			if (other.primary != null)
				return false;
		} else if (!primary.equals(other.primary))
			return false;
		if (secondaries == null) {
			if (other.secondaries != null)
				return false;
		} else if (!secondaries.equals(other.secondaries))
			return false;
		return true;
	}

}