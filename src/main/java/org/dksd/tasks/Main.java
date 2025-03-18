package org.dksd.tasks;

import org.dksd.tasks.pso.Domain;
import org.dksd.tasks.pso.FitnessFunction;
import org.dksd.tasks.pso.Particle;
import org.dksd.tasks.pso.StandardConcurrentSwarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class Main {

    public static void main(String[] args) throws IOException {

        //Need some logs...
        //What was the last thing we inferenced on,
        //When did we write the files to disk.
        //Which is the latest version?

        //Mostly avoid inference as much as possible.
        //watchFiles(coll);
        //onFilesChange(arethereHashDifferences?)->thenPersist;
        //beforeEachLLmInference(checkHashCache);
        //beforeEachLLmInference(checkEmbeddingCache);

        Collection coll = new Collection(new Instance("test"));
        TaskLLMProcessor taskLLMProcessor = new TaskLLMProcessor(coll);
        taskLLMProcessor.processSimpleTasks(parseTasks(Files.readAllLines(coll.getInstance().getTodoFilePath())));

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        while (!"q".equals(line)) {
            try {
                List<NodeTask> path = coll.getInlineTasks();
                TreeMap<Double, Integer> sorted = new TreeMap<>();
                for (NodeTask nodeTask : path) {
                    //TODO do we rather want deadlines with not a lot of time first...
                    //Probably.
                    //Work with lead time perhaps for calculating the start time. or just use that.
                    //sorted.put(coll.getInstance().getConstraint(nodeTask.getConstraints().getFirst()).getStartTime(),
                    //        nodeTask);
                }
                coll.displayTasks(path, sorted);
                System.out.print("Enter choice: ");
                line = reader.readLine();
                switch (line) {
                    case "/": // Search
                        System.out.print("Find: ");
                        coll.find(reader.readLine());
                        break;
                    case "p":
                        coll.setCurrentTask(path, i -> (i - 1 + path.size()) % path.size());
                        break;
                    case "n":
                        coll.setCurrentTask(path, i -> (i + 1) % path.size());
                        break;
                    case "": // Enter key
                        //selectTask();
                        System.out.println("Enter pressed");
                        break;
                    //case "csauto":
                    //    taskLLMProcessor.createAutoSubTaskFromParent(coll.getCurrentTask());
                    //    break;
                    case "cs":
                        multiInput(reader, (name, desc) -> coll.getInstance().createSubTask(coll.getCurrentTask(), name, desc));
                        break;
                    case "cd":
                        multiInput(reader, (name, desc) -> coll.getInstance().createDepTask(coll.getCurrentTask(), name, desc));
                        break;
                    case "e":
                        multiInput(reader, (name, desc) -> coll.getCurrentTask().updateTask(name, desc));
                        break;
                    case "d":
                        //deleteTask(coll.getCurrentTask());
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
        //Should I unroll all the tasks based on schedules...
        //
        StandardConcurrentSwarm swarm = new StandardConcurrentSwarm(new FitnessFunction() {
            @Override
            public double calcFitness(Particle p) {
                TreeMap<Double, Integer> sorted = new TreeMap<>();
                for (int i = 0; i < p.getGene().size(); i++) {
                    sorted.put(p.getGene().getValue(i), i);
                }
                //we loop through the week... executing tasks
                long time = 0;
                while (time <= 10080) {
                    //Do the next task

                    time+=1;//every minute
                }
                for (Map.Entry<Double, Integer> entry : sorted.entrySet()) {
                    Task task = coll.getInstance().getTasks().get(entry.getValue());

                }
                return 0;
            }

            @Override
            public int getDimension() {
                return coll.getTotalTaskCount();
            }

            @Override
            public List<Domain> getDomain() {
                List<Domain> domains = new ArrayList<>();
                for (int i = 0; i < coll.getTotalTaskCount(); i++) {
                    domains.add(new Domain(0.0, 1.0));
                }
                return domains;
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

    public static List<SimpleTask> parseTasks(List<String> lines) {
        List<SimpleTask> tasks = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.trim().isEmpty()) {
                continue;
            }

            // Count leading spaces to determine the indentation level.
            int indent = countLeadingSpaces(line);
            String trimmed = line.trim();

            // Remove a leading dash, if present.
            if (trimmed.startsWith("-")) {
                trimmed = trimmed.substring(1).trim();
            }

            SimpleTask parsedTask = new SimpleTask();
            try {
                parsedTask.taskName = trimmed.trim();
                //parsedTask = taskExtractor.extractTaskFrom(trimmed);
            } catch (Exception ep) {
                //NOOP
                parsedTask.taskName = trimmed.trim();
            }
            if (parsedTask.taskName == null) {
                parsedTask.taskName = trimmed.trim();
            }
            parsedTask.indent = indent;
            parsedTask.parentTask = getParent(lines, indent, i);
            parsedTask.line = i;
            tasks.add(parsedTask);
        }

        return tasks;
    }

    private static String getParent(List<String> lines, int indent, int i) {
        for (int j = i - 1; j >= 0; j--) {
            if (countLeadingSpaces(lines.get(j)) < indent) {
                return lines.get(j);
            }
        }
        return null;
    }

    // Utility method to count leading spaces in a string.
    private static int countLeadingSpaces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

}
