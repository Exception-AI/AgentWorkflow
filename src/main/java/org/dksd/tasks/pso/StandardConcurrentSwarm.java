package org.dksd.tasks.pso;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple concurrent swarm that updates all particles simultaneously.
 *
 * @author dscottdawkins
 */
public class StandardConcurrentSwarm implements Swarm {

    private int maxSteps = 1000;
    private Collection<Particle> particles = new ArrayList<>();
    private Gene gBest;
    private double gbestFitness;
    private final FitnessFunction ff;
    private AtomicLong lastGbestMove = new AtomicLong();
    private AtomicInteger step = new AtomicInteger();

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    /** See the full constructor description. */
    public StandardConcurrentSwarm(FitnessFunction ff, int numParticles) {
        this(ff, numParticles, Collections.emptyList());
    }

    /** See the full constructor description. */
    public StandardConcurrentSwarm(FitnessFunction ff, int numParticles, int maxSteps) {
        this(ff, numParticles, Collections.emptyList());
        this.maxSteps = maxSteps;
    }

    /**
     * Constructs a swarm given the fitness function, the number of particles and potentially some starting particles.
     *
     * @param ff fitness function
     * @param numParticles number of particles in this swarm
     * @param seedParticles beginning particles that could be good guesses or previous solutions
     */
    public StandardConcurrentSwarm(FitnessFunction ff, int numParticles, Collection<Particle> seedParticles) {
        this.ff = ff;
        particles.addAll(seedParticles);
        this.gBest = new Gene(ff.getDimension());
        for (int i = 0; i < numParticles; i++) {
            Particle particle = new Particle(ff);
            particles.add(particle);
            particle.init(ff.getDomain());
            if (i == 0) {
                particle.getGene().copyInto(gBest);
                gbestFitness = ff.calcFitness(particle);
            }
        }
    }

    /**
     * Updates all particles in the swarm based on global and neighbourhood bests.
     *
     * @return true if the swarm converged, false otherwise
     * @throws InterruptedException if the count down latch was interrupted
     */
    public boolean step() throws InterruptedException {
        //CountDownLatch cdl = new CountDownLatch(particles.size());
        for (Particle particle : this.particles) {
            //Runnable runnable = () -> {
                double partFitness = particle.update(getGbest());
                synchronized (gBest) {
                    if (partFitness < gbestFitness) {
                        particle.getGene().copyInto(gBest);
                        gbestFitness = partFitness;
                        long now = System.currentTimeMillis();
                        lastGbestMove.set(now);
                    }
                }
                //cdl.countDown();
            //};
            //executorService.submit(runnable);
        }
        //cdl.await();
        return (step.incrementAndGet() > maxSteps);
    }

    public Gene getGbest() {
        return gBest;
    }

    public double getGbestFitness() {
        return gbestFitness;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
