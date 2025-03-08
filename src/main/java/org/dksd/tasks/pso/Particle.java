package org.dksd.tasks.pso;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Represents a vector in space that flies through the search space with a velocity,
 * following the global and personal bests found so far.
 *
 * @author dscottdawkins
 */
public class Particle {

    /** Pay VERY careful attention to the random generator. There are better ones out there. */
    private static Random rand = new SecureRandom();

    private Gene pBest;
    private Gene particle;
    private Gene velocity;
    private double fitness;
    private double pfitness;
    private final FitnessFunction ff;

    private double w = 0.6;
    private double gbestconst = 0.9;
    private double pbestconst = 0.75;

    /** Ctor. */
    public Particle(FitnessFunction ff) {
        this.ff = ff;
        this.particle = new Gene(ff.getDimension());
        this.velocity = new Gene(ff.getDimension());
        this.pBest = new Gene(ff.getDimension());
    }

    public void init(double[] sadomain) {
        for (int i = 0; i < particle.size(); i++) {
            double rndv = pickRandom() * sadomain[i];
            particle.setValue(i, rndv);
            velocity.setValue(i, 0);
            pBest.setValue(i, particle.getValue(i));
        }
        pfitness = ff.calcFitness(this);
    }

    public double update(Gene gBest) {
        for (int i = 0; i < particle.size(); i++) {
            double r1 = pickRandom();
            double r2 = pickRandom();
            double v = w * velocity.getValue(i) + pbestconst * r1 * (pBest.getValue(i) - particle.getValue(i))
                    + gbestconst * r2 * (gBest.getValue(i) - particle.getValue(i));

            double domain = ff.getDomain()[i];
            v = constrainMaxVelocity(v, domain * 0.8);
            velocity.setValue(i, v);
            particle.setValue(i, particle.getValue(i) + v);
            constrainParticleToDomain(i, domain);
        }
        fitness = ff.calcFitness(this);
        if (fitness < pfitness) {
            this.getGene().copyInto(pBest);
            pfitness = fitness;
        }
        return fitness;
    }

    private void constrainParticleToDomain(int index, double domain) {
        if (particle.getValue(index) > domain) {
            particle.setValue(index, domain);
        }
        if (particle.getValue(index) < -domain) {
            particle.setValue(index, -domain);
        }
    }

    private double constrainMaxVelocity(double vel, double domain) {
        if (vel > domain) {
            vel = domain;
        }
        if (vel < -domain) {
            vel = -domain;
        }
        return vel;
    }

    private static double pickRandom() {
        return rand.nextDouble();
    }

    public double getFitness() {
        return fitness;
    }

    public Gene getGene() {
        return particle;
    }
}
