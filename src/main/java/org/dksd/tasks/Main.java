package org.dksd.tasks;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.dksd.tasks.model.LinkType;
import org.dksd.tasks.pso.FitnessFunction;
import org.dksd.tasks.pso.Particle;
import org.dksd.tasks.pso.StandardConcurrentSwarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

public class Main {


    public static void main(String[] args) throws IOException {
        Collection coll = new Collection(new Instance("schoolDiary"));
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                //.responseFormat(JSON)
                .modelName("deepseek-r1:latest")
                .build();

        ChatLanguageModel pojoModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .responseFormat(JSON)
                .modelName("mistral:latest")
                .build();

        List<String> lines = Files.readAllLines(coll.getInstance().getPath());
        //TaskExtractor taskExtractor = AiServices.create(TaskExtractor.class, model);
        ConstraintExtractor constraintExtractor = AiServices.create(ConstraintExtractor.class, pojoModel);
        List<SimpleTask> stasks = parseTasks(coll.getInstance(), lines);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        Map<String, Task> amp = new HashMap<>();
        for (SimpleTask stask : stasks) {
            String description = model.chat("Can you provide a description of the task name in 10-20 words: '" + stask.taskName + "' ?");
            Task task = new Task(stask.taskName, description);
            String schedule = model.chat("Can you take a guess at the scheduling of this task, when and how often we should execute or check this task: '" + task + "' also please append a cron expression of the schedule?");
            //Constraint constraint = new Constraint();
            task.getMetadata().put("schedule", schedule);
            task.getMetadata().put("fileName", coll.getInstance().getPath().toString());
            task.getMetadata().put("lineNumber", stask.line);
            coll.getInstance().addTask(task);
            System.out.println("Task: " + task.getName() + " id: " + task.getId());
            amp.put(task.getName(), task);
            try {
                Constraint constraint = constraintExtractor.extractConstraintFrom(task.toString());
                coll.getInstance().addConstraint(task, constraint);
                coll.getInstance().write(coll);
            } catch (Exception ep) {
                ep.printStackTrace();
            }
        }
        for (SimpleTask stask : stasks) {
            Task parent = amp.get(stask.parentTask);
            Task child = amp.get(stask.taskName);
            if (parent == null && child != null) {
                coll.getInstance().addLink(null, LinkType.PARENT, child.getId());
            }
            if (parent != null && child != null) {
                coll.getInstance().addLink(parent.getId(), LinkType.PARENT, child.getId());
                coll.getInstance().addLink(parent.getId(), LinkType.SUBTASK, child.getId());
            }
        }

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

    public static List<SimpleTask> parseTasks(Instance instance, List<String> lines) {
        List<SimpleTask> tasks = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            boolean exists = false;
            for (Task task : instance.getTasks()) {
                if (task.getMetadata().isEmpty()) {
                    continue;
                }
                if (task.getMetadata().get("fileName").equals(instance.getPath().toString()) && task.getMetadata().get("lineNumber").equals(i)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                continue;
            }
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
            }
            catch (Exception ep) {
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
            if (c == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

}
