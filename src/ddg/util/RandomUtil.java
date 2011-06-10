/**
 * 
 */
package ddg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * It implements a random 
 * 
 * @author thiagoepdc
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class RandomUtil {

	/**
	 * @param samplesCount
	 * @param n exclusive
	 * @return
	 */
	public static List<Integer> random(int samplesCount, int n){
		
		if( samplesCount < 0 ) throw new IllegalArgumentException("samplesCount cannot be negative: "+samplesCount);
		
		//n is exclusive, so <=
		if( (n <= 0) || (n <= samplesCount) ) throw new IllegalArgumentException("ceil value must be positive " +
				"and greate than samples count: n <"+n+"> samplesCount<"+samplesCount+">");
		
		Random random = new Random();
		
		List<Integer> samples = new ArrayList<Integer>();
		
		while ( samples.size() != samplesCount) {
			
			int randomValue = random.nextInt(n);
			if(!samples.contains(randomValue)) {
				samples.add(randomValue);
			}
		}
		
		return samples;
	}

	/**
	 * It returns a list containing unsorted integers between 0 (inclusive) and n (exclusive).
	 * The values do not appear more than one time in the list. 
	 * 
	 * @param n Upper bound (excluded).
	 * @return A list of n integers.
	 */
	public static List<Integer> random( int n ){
		
		//n is exclusive, so <=
		if( (n <= 0) ) 
			throw new IllegalArgumentException("ceil value must be positive: n <" +	n + ">");
		
		Random random = new Random();
		
		List<Integer> samples = new ArrayList<Integer>();
		while(samples.size() < n) {
			int randomValue = random.nextInt(n);
			if(!samples.contains(randomValue)) {
				samples.add(randomValue);
			}
		}
		return samples;
	}
}
