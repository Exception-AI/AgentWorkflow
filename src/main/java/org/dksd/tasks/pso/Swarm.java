package org.dksd.tasks.pso;

/**
 * Implement this interface to control a number of particles that are aware of the neighbourhood and global best solution.
 *
 * @author dscottdawkins
 */
public interface Swarm {

   /** Steps through one full update for all particles. Returns true if converged false otherwise. */
   boolean step() throws InterruptedException;

   /** Gets the global best values found so far. */
   Gene getGbest();

   /** Gets the global best fitness found so far. Smaller is better. */
   double getGbestFitness();

}
