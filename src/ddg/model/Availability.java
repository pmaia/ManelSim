package ddg.model;

import ddg.kernel.JETime;

/**
 * 
 * Represents the (un)availability of one machine
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public interface Availability {
	
	/**
	 * Sets the simulation time to the given {@link JETime}. 
	 * Subsequent calls to this method must use crescent values for now. 
	 * 
	 * @param now the current simulation time
	 * @throws IllegalArgumentException if the given {@link JETime} is before the one of the last call
	 */
	public void updateSimulationTime(JETime now);
	
	public JETime getSimulationTime();
	
	/**
	 * 
	 * @return
	 */
	public boolean isAvailable();
	
}
