package org.dksd.tasks.pso;

/**
 * Simple array of doubles to represent a possible solution as measured by the fitness function.
 *
 * @author dscottdawkins
 */
public class Gene {

    /** Array of values representing the solution. */
   private double[] gene;

   /** The dimension of the problem. */
   private final int dimension;

   /** Constructs an instance of a Gene. */
   public Gene(int dimension) {
      this.dimension = dimension;
      gene = new double[dimension];
   }

   /** Deep copies one gene to another. */
   public void copyInto(Gene g) {
      for (int i = 0; i < this.dimension; i++) {
         g.setValue(i, gene[i]);
      }
   }

    /** {@inheritDoc} */
   @Override
   public String toString() {
      StringBuilder result = new StringBuilder("");
      for (int i = 0; i < this.dimension; i++) {
         result.append(getValue(i)).append(", ");
      }
      return result.toString();
   }

   /** Gets the value at a position. */
    public double getValue(int pos) {
        return gene[pos];
    }

    /** Sets the value at a given position. */
    public void setValue(int pos, double value) {
        gene[pos] = value;

    }

    /** Gets the number of values or dimension. */
    public int size() {
        return dimension;
    }
}
