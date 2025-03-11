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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

public class Main {


    public static void main(String[] args) {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .responseFormat(JSON)
                .modelName("deepseek-r1:latest")
                .build();

        TaskExtractor taskExtractor = AiServices.create(TaskExtractor.class, model);
        List<SimpleTask> stasks = parseTasks(taskExtractor, """
                School Kids
                  - Calendar
                    - April: 7th - 11th Holiday/Recess no school
                    - May: 26th Is a holiday so no school
                    - June: 13th Minimum day Last day of school, 16th, 17th, 18th, 19th Teacher work day and holidays no school.
                    - July: 4th Holiday
                    - August: 19th-21st Teacher days no school. First day of school is 22nd August
                    - September: 2nd Holiday (Labor day)
                    - October: 3rd Holiday
                    - November: 1st,11,25th - 29th Holidays.
                    - December: 23rd - 31st Holidays
                  - Homework due Monday
                    - Reading homework every night
                    - Extra Spelling and Math practice every two days
                  - Clothes washed
                  - Bath every two nights
                  - Field Trips
                    - Organize Driving school docs so I can drive
                  - After school activities, like cabaret
                  - After school play dates
                  - After school unpack snacks and lunch
                  - Odyssey of the Mind on Monday mornings
                  - Any birthday parties?
                  - Bike to school on Wednesday mornings
                  - School starts at 8:15am but usually leave the house at 7:45am
                  - School pickup is on 2:35pm on Mon, Tue, Thur, Fri. On Wednesday it is early pickup at 1:45pm
                    - 9:55am - 10:10am recess
                    - 11:40am - 12:20pm Lunch
                    - 1:45pm - 1:55pm Recess
                  - Tamalpais Valley School website is: 415-389-7731 and email is: atrapp@mvschools.org and address is: 350 Bell Lane, Mill Valley, CA 94941
                  - Update schedule for when there is no school etc. see: www.mvschools.org/Page/8920
                """);

        Collection coll = new Collection(new Instance("schoolDiary"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        Map<String, Task> amp = new HashMap<>();
        for (SimpleTask stask : stasks) {
            Task task = new Task(stask.taskName, stask.description);
            coll.getInstance().addTask(task);
            System.out.println("Task: " + task.getName() + " id: " + task.getId());
            amp.put(task.getName(), task);
        }
        for (SimpleTask stask : stasks) {
            Task parent = amp.get(stask.parentTask);
            Task child = amp.get(stask.taskName);
            if (parent != null && child != null) {
                coll.getInstance().addLink(parent.getId(), LinkType.PARENT, child.getId());
            }
        }

        System.out.println(coll.getInstance().getTasks());

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

    public static List<SimpleTask> parseTasks(TaskExtractor taskExtractor, String text) {
        List<SimpleTask> tasks = new ArrayList<>();
        Stack<SimpleTask> stack = new Stack<>();
        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
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

            // Pop from the stack until we find a task with a lower indentation level.
            while (!stack.isEmpty() && stack.peek().indent >= indent) {
                stack.pop();
            }
            // The parent is the task on top of the stack (if any).
            String parentName = stack.isEmpty() ? null : stack.peek().taskName;

            SimpleTask parsedTask = null;
            try {
                parsedTask = taskExtractor.extractTaskFrom(trimmed);
            }
            catch (Exception ep) {
                //NOOP
                parsedTask = new SimpleTask();
                parsedTask.taskName = trimmed.trim();
            }
            if (parsedTask.taskName == null) {
                parsedTask.taskName = trimmed.trim();
            }
            parsedTask.indent = indent;
            parsedTask.parentTask = parentName;
            tasks.add(parsedTask);
            stack.push(parsedTask);
        }

        return tasks;
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
