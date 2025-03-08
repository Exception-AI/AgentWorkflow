package org.dksd.tasks;

import org.dksd.tasks.pso.FitnessFunction;
import org.dksd.tasks.pso.Particle;
import org.dksd.tasks.pso.StandardConcurrentSwarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {

        Instance instance = new Instance("school_diary");
        Map<Instance, Collection> collections = new HashMap<>();
        collections.put(instance, new Collection());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        Collection collection = collections.get(instance);

        while (!"q".equals(line)) {
            try {
                collection.displayTasks();

                System.out.print("Enter choice: ");
                line = reader.readLine();

                switch (line) {
                    case "/": // Search
                        System.out.print("Find: ");
                        collection.find(reader.readLine());
                        break;
                    case "o":
                        instance.setCurrentTaskToParent();
                        break;
                    case "n":
                        instance.setCurrentTaskToNext(instance.getCurrentNodeTask().getId());
                        break;
                    case "": // Enter key
                        //selectTask();
                        System.out.println("Enter pressed");
                        break;
                    case "cp":
                        collection.multiInput(reader, instance::createProjectTask);
                        break;
                    case "cs":
                        collection.multiInput(reader, (name, desc) -> instance.createSubTask(instance.getCurrentTask(), name, desc));
                        break;
                    case "cd":
                        collection.multiInput(reader, (name, desc) -> instance.createDepTask(instance.getCurrentTask(), name, desc));
                        break;
                    case "e":
                        collection.multiInput(reader, (name, desc) -> instance.getCurrentTask().updateTask(name, desc));
                        break;
                    case ":w": // Write
                        instance.write(collection);
                        break;
                    case "q": // Quit
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        instance.write(collection);
        StandardConcurrentSwarm swarm = new StandardConcurrentSwarm(new FitnessFunction() {
            @Override
            public double calcFitness(Particle p) {
                //sort p according to value and index.
                //then go through tasks int that order and calc fitness.
                Set<Task> depCache = new HashSet<>();

                TreeMap<Double, Integer> sorted = new TreeMap<>();
                for (int i = 0; i < p.getGene().size(); i++) {
                    sorted.put(p.getGene().getValue(i), i);
                }
                for (Map.Entry<Double, Integer> entry : sorted.entrySet()) {
                    Task task = instance.getTasks().get(entry.getValue());
                    System.out.println("Task ordering: " + task);
                    //Weighted by importance, effort, cost
                    //get next execution time of task.
                    //Deadline calc
                }
                return 0;
            }

            @Override
            public int getDimension() {
                return instance.getTasks().size();
            }

            @Override
            public double[] getDomain() {
                double[] dm = new double[instance.getTasks().size()];
                for (int i = 0; i < instance.getTasks().size(); i++) {
                    dm[i] = 1.0;
                }
                return dm;
            }
        }, 10);
        try {
            for (int i = 0; i < 5; i++) {
                swarm.step();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}