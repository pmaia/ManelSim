package ddg.model;

public class State {
	
	private boolean active;
	
	private long duration;

	public State(boolean active, long duration) {
		super();
		this.active = active;
		this.duration = duration;
	}

	public boolean isActive() {
		return active;
	}

	public long getDuration() {
		return duration;
	}
	
}
