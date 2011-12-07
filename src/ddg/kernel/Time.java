package ddg.kernel;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 * @author Thiago Emmanuel - thiagoepdc@lsd.ufcg.edu.br
 */
public final class Time implements Comparable<Time> {
	
	public static final Time GENESIS = new Time(0, Unit.MICROSECONDS);
	public static final Time END_OF_THE_WORLD = new Time(Long.MAX_VALUE, Unit.MICROSECONDS); 

	public enum Unit {SECONDS, MILLISECONDS, MICROSECONDS}
	
	private final long timeMicroSeconds;

	public Time(long time, Unit unit) {
		switch(unit) {
		case SECONDS: this.timeMicroSeconds = time * 1000000; break;
		case MILLISECONDS: this.timeMicroSeconds = time * 1000; break;
		case MICROSECONDS: this.timeMicroSeconds = time; break;
		default:
			throw new IllegalArgumentException("Impossible argument exception");
		}
	}

	/**
	 * @param otherETime
	 * @return
	 */
	public Time plus(Time otherETime) {
		return new Time(timeMicroSeconds + otherETime.timeMicroSeconds, Unit.MICROSECONDS);
	}
	
	public Time minus(Time otherETime) {
		return new Time(timeMicroSeconds - otherETime.timeMicroSeconds, Unit.MICROSECONDS);
	}
	
	public Time times(int multiplier) {
		return new Time(timeMicroSeconds * multiplier, Unit.MICROSECONDS);
	}
	
	public long asMicroseconds() {
		return timeMicroSeconds;
	}
	
	public long asMilliseconds() {
		return asMicroseconds() / 1000;
	}
	
	public long asSeconds() {
		return asMilliseconds() / 1000;
	}
	
	public long asMinutes() {
		return asSeconds() / 60;
	}
	
	public long asHours() {
		return asMinutes() / 60;
	}

	/**
	 * @param otherTime
	 * @return
	 */
	public boolean isEarlierThan(Time otherTime) {
		return (compareTo(otherTime) < 0);
	}

	/**
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Time otherTime) {
		long diff = timeMicroSeconds - otherTime.timeMicroSeconds;
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Time))
			return false;
		
		Time otherTime = (Time)obj;
		
		return otherTime.timeMicroSeconds == timeMicroSeconds;
	}
	
	@Override
	public int hashCode() {
		return (int)timeMicroSeconds;
	}

	/**
	 * Returns the String that represents this {@link Time} as milliseconds
	 */
	@Override
	public String toString() {
		return String.valueOf(timeMicroSeconds);
	}
}