package org.dksd.tasks.pso;

/**
 *
 * @author dscottdawkins
 */
public interface FitnessFunction {

   double calcFitness(Particle p);

   int getDimension();

   double[] getDomain();

}
