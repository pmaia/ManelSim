/* JETime - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package ddg.kernel;

/**
 * TODO make doc
 *
 * @author thiago - thiago@lsd.ufcg.edu.br
 */
public final class JETime implements Comparable<JETime> {

	//FIXME: CREATE A STATIC SUBCLASS TO MODEL AN INFINITY JETIME
	
    private final long timeMilliSeconds;
    
    /**
     * @param aTime
     */
    public JETime(long timeMilliSeconds) {
    	this.timeMilliSeconds = timeMilliSeconds;
    }
    
    /**
     * @param otherETime
     * @return
     */
    public JETime plus(JETime otherETime) {
    	return new JETime(timeMilliSeconds + otherETime.timeMilliSeconds);
    }
    
    /**
     * @param otherTime
     * @return
     */
    public boolean isEarlierThan(JETime otherTime) {
		return (compareTo(otherTime) < 0);
    }
    
    /**
     * @param o
     * @return
     */
    public int compareTo(JETime o) {
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
    	return timeMilliSeconds + "";//FIXME optimize
    }
}