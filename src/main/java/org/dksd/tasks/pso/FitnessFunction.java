package org.dksd.tasks.pso;

import java.util.List;

/**
 *
 * @author dscottdawkins
 */
public interface FitnessFunction {

   double calcFitness(Particle p);

   int getDimension();

   List<Domain> getDomain();

}
