package manelsim;

public abstract class RepeatableEvent extends Event {
	
	private Time interval;
	private Time mutableScheduledTime;

	public RepeatableEvent(Time firstTime, Time interval) {
		super(firstTime);
		this.mutableScheduledTime = firstTime;
		this.interval = interval;
	}

	@Override
	public void process() {
		work();
		mutableScheduledTime = mutableScheduledTime.plus(interval);
		EventScheduler.schedule(this);
	}
	
	public Time getScheduledTime() {
		return mutableScheduledTime;
	}
	
	public abstract void work();

}
