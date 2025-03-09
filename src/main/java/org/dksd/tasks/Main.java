package org.dksd.tasks;

import org.dksd.tasks.pso.FitnessFunction;
import org.dksd.tasks.pso.Particle;
import org.dksd.tasks.pso.StandardConcurrentSwarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class Main {
    public static void main(String[] args) {

        Collection coll = new Collection(new Instance("school_diary"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;


        while (!"q".equals(line)) {
            try {
                coll.displayTasks();
                System.out.print("Enter choice: ");
                line = reader.readLine();

                switch (line) {
                    case "/": // Search
                        System.out.print("Find: ");
                        coll.find(reader.readLine());
                        break;
                    case "o":
                        coll.setCurrentTaskToParent();
                        break;
                    case "n":
                        coll.setCurrentTaskToNext();
                        break;
                    case "": // Enter key
                        //selectTask();
                        System.out.println("Enter pressed");
                        break;
                    case "cs":
                        multiInput(reader, (name, desc) -> coll.getInstance().createSubTask(coll.getCurrentTask(), name, desc));
                        break;
                    case "cd":
                        multiInput(reader, (name, desc) -> coll.getInstance().createDepTask(coll.getCurrentTask(), name, desc));
                        break;
                    case "e":
                        multiInput(reader, (name, desc) -> coll.getCurrentTask().updateTask(name, desc));
                        break;
                    case ":w": // Write
                        coll.getInstance().write(coll);
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
        coll.getInstance().write(coll);
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
                    //Task task = instance.getTasks().get(entry.getValue());
                    //System.out.println("Task ordering: " + task);
                    //Weighted by importance, effort, cost
                    //get next execution time of task.
                    //Deadline calc
                }
                return 0;
            }

            @Override
            public int getDimension() {
                return 0;//instance.getTasks().size();
            }

            @Override
            public double[] getDomain() {
                /*double[] dm = new double[instance.getTasks().size()];
                for (int i = 0; i < instance.getTasks().size(); i++) {
                    dm[i] = 1.0;
                }
                return dm;*/
                return new double[0];
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

    public static void multiInput(BufferedReader reader, BiConsumer<String, String> updateFunction) throws IOException {
        System.out.print("Edit name: ");
        String name = reader.readLine();
        System.out.print("Edit description: ");
        String desc = reader.readLine();
        updateFunction.accept(name, desc);
    }
}