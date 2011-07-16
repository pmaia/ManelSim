package ddg.model;


/**
 * 
 * Represents the (un)availability of one machine
 * 
 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 *
 */
public interface Availability {
	
	State currentState();
	
	void advanceState();
	
}
