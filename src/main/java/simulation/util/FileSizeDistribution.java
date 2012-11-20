package simulation.util;

import eduni.distributions.LogNormal;

/**
 * @author Thiago Emmanuel, thiagoepdc@lsd.ufcg.edu.br
 */
public class FileSizeDistribution {

	private final double maxSize;
	private final LogNormal logNormal;

	public FileSizeDistribution(double mean, double variance, double maxSize) {
		this.maxSize = maxSize;
		logNormal = new LogNormal(mean, variance);
	}

	public double nextSampleSize() {
		double sample = logNormal.sample();
		return Math.min(sample, maxSize);
	}

}