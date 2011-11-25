/* JETime - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package ddg.kernel;

/**
 * 
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public final class Time implements Comparable<Time> {

	private final long timeMilliSeconds;
	
	public enum Unit {SECONDS, MILLISECONDS, MICROSECONDS}

	public Time(long time, Unit unit) {
		switch(unit) {
		case SECONDS: this.timeMilliSeconds = time * 1000; break;
		case MILLISECONDS: this.timeMilliSeconds = time; break;
		case MICROSECONDS: this.timeMilliSeconds = time / 1000; break;
		default:
			throw new IllegalArgumentException("Impossible argument exception");
		}
	}

	/**
	 * @param otherETime
	 * @return
	 */
	public Time plus(Time otherETime) {
		return new Time(timeMilliSeconds + otherETime.timeMilliSeconds, Unit.MILLISECONDS);
	}
	
	public Time minus(Time otherETime) {
		return new Time(timeMilliSeconds - otherETime.timeMilliSeconds, Unit.MILLISECONDS);
	}
	
	public long asMilliseconds() {
		return timeMilliSeconds;
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
		long diff = timeMilliSeconds - o.timeMilliSeconds;
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.valueOf(timeMilliSeconds);
	}
}