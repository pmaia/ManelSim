package manelsim;

public abstract class Event implements Comparable<Event> {

	private final Time scheduledTime;
	
	private boolean processed = false;

	public Event(Time scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public abstract void process();

	public Time getScheduledTime() {
		return scheduledTime;
	}
	
	public void setProcessed() {
		processed = true;
	}
	
	public boolean wasProcessed() {
		return processed;
	}

	@Override
	public int compareTo(Event o) {
		return this.getScheduledTime().compareTo(o.getScheduledTime());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getScheduledTime() == null) ? 0 : getScheduledTime().hashCode());
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
		Event other = (Event) obj;
		if (getScheduledTime() == null) {
			if (other.getScheduledTime() != null)
				return false;
		} else if (!getScheduledTime().equals(other.getScheduledTime()))
			return false;
		return true;
	}
	
}