package ddg.util;

import eduni.distributions.LogNormal;

/**
 * TODO make doc
 *
 * @author Thiago Emmanuel, thiagoepdc@lsd.ufcg.edu.br
 */
public class FileSizeDistribution {

	private final LogNormal logNormal;
	private final double maxSize;

	/**
	 * @param mean
	 * @param variance
	 * @param maxSize TODO
	 */
	public FileSizeDistribution(double mean, double variance, double maxSize) {
		this.maxSize = maxSize;
		logNormal = new LogNormal(mean, variance);
	}

	/**
	 * @return
	 */
	public double nextSampleSize() {
		double sample = logNormal.sample();
		return Math.min(sample, maxSize);
	}

}