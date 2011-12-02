package ddg.kernel;

/**
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 * @author Thiago Emmanuel - thiagoepdc@lsd.ufcg.edu.br
 */
public final class Time implements Comparable<Time> {
	
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
		return new Time(timeMicroSeconds + otherETime.timeMicroSeconds, Unit.MILLISECONDS);
	}
	
	public Time minus(Time otherETime) {
		return new Time(timeMicroSeconds - otherETime.timeMicroSeconds, Unit.MILLISECONDS);
	}
	
	public Time times(int multiplier) {
		return new Time(timeMicroSeconds * multiplier, Unit.MILLISECONDS);
	}
	
	public long asMilliseconds() {
		return timeMicroSeconds / 1000;
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
	public int compareTo(Time o) {
		double diff = timeMicroSeconds - o.timeMicroSeconds;
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		}
		return 0;
	}

	/**
	 * Returns the String that represents this {@link Time} as milliseconds
	 */
	@Override
	public String toString() {
		return String.valueOf(asMilliseconds());
	}
}