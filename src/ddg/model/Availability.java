package ddg.model;

/**
 * 
 * Represents an (un)availability distribution for one machine
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public interface Availability {
	
	/**
	 * @return the next availability duration for one machine in milliseconds
	 */
	public long nextAvailabilityDuration();
	
	/**
	 * @return the next unavailability duration for one machine in milliseconds
	 */
	public long nextUnavailabilityDuration();
	
}
