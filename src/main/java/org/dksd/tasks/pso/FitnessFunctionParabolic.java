package org.dksd.tasks.pso;

/**
 *
 * @author dscottdawkins
 */
public class FitnessFunctionParabolic implements FitnessFunction {

	public double calcFitness(Particle p) {
		return p.getGene().getValue(0) * p.getGene().getValue(0);
	}

	public int getDimension() {
		return 1;
	}

	public double[] getDomain() {
		double[] domain = new double[getDimension()];
		for (int i = 0; i < domain.length; i++) {
			domain[i] = 1000;
		}
		return domain;
	}
}
