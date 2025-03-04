package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        List<Task> tasks = loadTasks("tasks.json");
        List<Link> links = loadLinks("links.json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        Map<Long, Task> fullMap = new HashMap<>();
        tasks.forEach(task -> {fullMap.put(task.getId(), task);});

        Set<Task> workingSet = new HashSet<>();
        Map<Long, NodeTask> tree = null;
        selectTasks(fullMap, workingSet);
        int currentIndex = 0;

        while (!"q".equals(line)) {
            try {
                fullMap.clear();
                tasks.forEach(task -> {fullMap.put(task.getId(), task);});
                tree = buildTree(tasks, links);
                displayTasks(tree, fullMap, workingSet);

                    System.out.println("Enter choice: ");
                    line = reader.readLine();


                switch (line) {
                    case "k": // Move up
                        currentIndex++;
                        break;
                    case "j": // Move down
                        currentIndex--;
                        break;
                    case "\r": // Enter key
                        //selectTask();
                        break;
                    case "c":
                        System.out.println("Enter name: ");
                        String name = reader.readLine();
                        System.out.println("Enter description: ");
                        String desc = reader.readLine();
                        createTask(name, desc);
                        break;
                    case "q": // Quit
                       break;
                    default:
                        break;
                }
                selectTasks(fullMap, workingSet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /*Task featureDev = new Task("review spec", "looks for certain things: focus on outcomes, interfaces/missing specs/",
             Creator.of(Task.of("", "")),
                Creator.of(Task.of("", "")));*/


        /*        .addTask("add unit tests", "unit tests template/checklist, large datasets", new TaskBuilder().build())
                .addTask("write the code", "things to look for, constants, DRY etc", new Task.Builder().build())
                .addTask("testing the code", "testing testing, match outcomes? large datasets, experiment env, logs, loom, links, any regressions etc ", new Tasks.Builder().build())
                .addTask("review the code", "things to look for, db indexes, simpler impls, " +
                "AI code review step, linting, other tools+tests", new Tasks.Builder().build())
                .addTask("PR template", "<PR template> goes here", new Tasks.Builder().build())
                .build();
*/
        //could create a new instance... for a new branch
        //need a wrapper object.
        //all.load(); //indicates state which tasks are completed versus not etc
        // show parent, go into or out of up/down.
        // Be good to print out too and can see.
        writeJson("tasks.json", toJson(tasks));
        writeJson("links.json", toJson(links));
    }

    private static void createTask(String name, String desc) {

    }

    private static Map<Long, NodeTask> buildTree(List<Task> tasks, List<Link> links) {
        Map<Long, NodeTask> taskMap = new HashMap<>();
        for (Task task : tasks) {
            NodeTask t = new NodeTask(task.getId());
            taskMap.put(task.getId(), t);
        }
        for (Link link : links) {
            if (LinkType.PARENT.equals(link.getLinkType())) {
                taskMap.get(link.getRight()).setParentId(link.getLeft());
            }
            if (LinkType.CHILD.equals(link.getLinkType())) {
                taskMap.get(link.getLeft()).setParentId(link.getRight());
            }
            if (LinkType.SUBTASK.equals(link.getLinkType())) {
                taskMap.get(link.getLeft()).getSubTasks().add(link.getRight());
            }
            if (LinkType.DEPENDENCY.equals(link.getLinkType())) {
                taskMap.get(link.getLeft()).getDependencies().add(link.getRight());
            }
        }
        return taskMap;
    }

    public static String toJson(List<?> tasks) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void displayTasks(Map<Long, NodeTask> tree, Map<Long, Task> fullMap, Set<Task> workingSet) {
        for (Task wt : workingSet) {
            NodeTask p = tree.get(wt.getId());
            while (p.getParentId() != null) {
                System.out.println(fullMap.get(wt.getId()).getName() + " -> ");
                p = tree.get(p.getParentId());
            }

            System.out.println("Task: " + wt.getName() + " : " + wt.getDescription());

            for (Long subTask : tree.get(wt.getId()).getSubTasks()) {
                System.out.println("  SubTasks: ");
                System.out.println("      - " + fullMap.get(subTask).getName() + " : " + fullMap.get(subTask).getDescription());
            }
            for (Long dep : tree.get(wt.getId()).getDependencies()) {
                System.out.println("  Dependencies: ");
                System.out.println("      - " + fullMap.get(dep).getName() + " : " + fullMap.get(dep).getDescription());
            }
        }
    }

    public static List<Task> loadTasks(String fileName) {
        // Create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Replace "task.json" with the path to your JSON file.
            File jsonFile = new File(fileName);

            // Deserialize JSON into a Task object.
            List<Task> loadedTasks = mapper.readValue(jsonFile, new TypeReference<List<Task>>(){});

            System.out.println("Loaded tasks");
            return loadedTasks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<Link> loadLinks(String fileName) {
        // Create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Replace "task.json" with the path to your JSON file.
            File jsonFile = new File(fileName);

            // Deserialize JSON into a Task object.
            List<Link> loadedTasks = mapper.readValue(jsonFile, new TypeReference<List<Link>>(){});

            System.out.println("Loaded tasks");
            return loadedTasks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void writeJson(String fileName, String json) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
            fileWriter.flush();
            System.out.println("Successfully saved JSON to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void selectTasks(Map<Long, Task> fullMap, Set<Task> workingSet) {
        //TODO this is where it can get more fancy
        if (fullMap.isEmpty()) {
            return;
        }
        Random rand = new Random();
        List<Long> indexes = new ArrayList<>(fullMap.keySet());
        for (int i = 0; i < 10 && workingSet.size() < 10; i++) {
            int ii = rand.nextInt(indexes.size());
            workingSet.add(fullMap.get(indexes.get(ii)));
        }
    }
}