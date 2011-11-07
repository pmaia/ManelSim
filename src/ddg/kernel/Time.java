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

	/**
	 * @param aTime
	 */
	public Time(long timeMilliSeconds) {
		this.timeMilliSeconds = timeMilliSeconds;
	}

	/**
	 * @param otherETime
	 * @return
	 */
	public Time plus(Time otherETime) {
		return new Time(timeMilliSeconds + otherETime.timeMilliSeconds);
	}
	
	public Time minus(Time otherETime) {
		return new Time(timeMilliSeconds - otherETime.timeMilliSeconds);
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