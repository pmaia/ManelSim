/**
 * 
 */
package ddg.model.data;

import java.util.Set;

import ddg.kernel.Time;
import ddg.kernel.Time.Unit;

public class ReplicationGroup {

	private final String fileName;
	private final DataServer primary;
	private final Set<DataServer> secondaries;
	
	private Time totalChangesDuration = new Time(0, Unit.SECONDS);
	
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

	public void addChangesDuration(Time changeDuration) {
		totalChangesDuration = totalChangesDuration.plus(changeDuration);
	}
	
	public Time getTotalChangesDuration() {
		return totalChangesDuration;
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