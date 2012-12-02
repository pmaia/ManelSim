package manelsim;

/**
 * 
 * @author Patrick Maia
 *
 */
public class TimeInterval {
	
	private final Time begin;
	private final Time end;

	public TimeInterval(Time begin, Time end) {
		if(end.isEarlierThan(begin)) {
			throw new IllegalArgumentException("Interval must begin before end.");
		}
		this.begin = begin;
		this.end = end;
	}
	
	public Time begin() {
		return begin;
	}

	public Time end() {
		return end;
	}

	public boolean overlaps(TimeInterval otherInterval) {
		if(begin.compareTo(otherInterval.begin) <= 0 ) {
			return contains(otherInterval.begin);
		} else {
			return otherInterval.contains(begin);
		}
	}
	
	/**
	 * Returns true if otherInterval is contiguous to this one. 
	 * Two {@link TimeInterval}s are contiguous if the end of one is equal to the begin of the other.
	 */
	public boolean isContiguous(TimeInterval otherInterval) {
		return (end.equals(otherInterval.begin) || begin.equals(otherInterval.end));
	}

	private boolean contains(Time time) {
		return (begin.compareTo(time) <= 0) && (time.compareTo(end) <= 0);
	}
	
	/**
	 * Merge two overlapping intervals.
	 * 
	 * @param otherInterval
	 * @return the intervals merged
	 */
	public TimeInterval merge(TimeInterval otherInterval) {
		if(!overlaps(otherInterval))
			throw new IllegalArgumentException("It is not possible to merge non overlapping intervals.");
		
		Time resultBegin = (begin.compareTo(otherInterval.begin) <= 0) ? begin : otherInterval.begin;
		Time resultEnd = (end.compareTo(otherInterval.end) >= 0) ? end : otherInterval.end;
		
		return new TimeInterval(resultBegin, resultEnd);
	}
	
	public TimeInterval intersection(TimeInterval otherInterval) {
		TimeInterval interval = null;
		if(overlaps(otherInterval)) {
			Time begin = this.begin.isEarlierThan(otherInterval.begin) ? otherInterval.begin : this.begin;
			Time end = this.end.isEarlierThan(otherInterval.end) ? this.end : otherInterval.end;
			interval = new TimeInterval(begin, end);
		}
		return interval;
	}
	
	public TimeInterval [] diff(TimeInterval otherInterval) {
		TimeInterval [] result = new TimeInterval[2];
		int i = 0;
		if(overlaps(otherInterval)) {
			TimeInterval intersection = intersection(otherInterval);
			if(!equals(intersection)) { 
				// if this interval were equal to intersection then this interval is a subset of otherInterval and the diff is an empty set
				if(begin.isEarlierThan(intersection.begin)) {
					result[i++] = new TimeInterval(begin, intersection.begin); // part 1
				}
				if(intersection.end.isEarlierThan(end)) {
					result[i] = new TimeInterval(intersection.end, end); // part 2
				}
			}
		} else {
			result[i] = this;
		}
		return result;
	}
	
	public Time delta() {
		return end.minus(begin);
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", begin, end);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((begin == null) ? 0 : begin.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
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
		TimeInterval other = (TimeInterval) obj;
		if (begin == null) {
			if (other.begin != null)
				return false;
		} else if (!begin.equals(other.begin))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		return true;
	}

}
