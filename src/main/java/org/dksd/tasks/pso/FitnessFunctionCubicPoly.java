package org.dksd.tasks.pso;

/**
 *
 * @author dscottdawkins
 */
public class FitnessFunctionCubicPoly implements FitnessFunction {

	private final double[] xi;
	private final double [] yi;
	
	public FitnessFunctionCubicPoly(final double[] xi, final double [] yi) {
		this.xi = xi;
		this.yi = yi;
	}

	public double calcFitness(Particle p) {
	   /*System.out.println("x values: ");
	   for (int i = 0; i < xi.length; i++) {
	      System.out.print(xi[i]+",");
	   }
	   System.out.println();
	   System.out.println("y values: ");
	   for (int i = 0; i < xi.length; i++) {
         System.out.print(yi[i]+",");
      }
	   System.out.println();*/
		double error = 0;
		for (int i = 0; i < xi.length; i++) {
			error = error
					+ Math.abs(yi[i]
							- fox(p.getGene().getValue(0), p.getGene()
									.getValue(1), p.getGene().getValue(2), p
									.getGene().getValue(3), xi[i]));
		}
		return error;
	}

	private double fox(double a, double b, double c, double d, double x) {
		double x2 = x * x;
		double x3 = x2 * x;
		return a * x2 + b * x + c;
		//return a * x3 + b * x2 + c * x + d;
	}

	public int getDimension() {
		return 4;
	}

	public double[] getDomain() {
		double[] domain = new double[getDimension()];
		for (int i = 0; i < domain.length; i++) {
			domain[i] = 100;
		}
		return domain;
	}

}
