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
        tasks.forEach(task -> {
            fullMap.put(task.getId(), task);
        });

        Set<Task> workingSet = new HashSet<>();
        Map<Long, NodeTask> tree = buildTree(tasks, links);;
        selectTasks(fullMap, workingSet);
        NodeTask currentTask = (!workingSet.isEmpty()) ? tree.get(workingSet.stream().findFirst().get().getId()) : null;

        while (!"q".equals(line)) {
            try {
                fullMap.clear();
                tasks.forEach(task -> {
                    fullMap.put(task.getId(), task);
                });
                tree = buildTree(tasks, links);
                displayTasks(tree, fullMap, workingSet);

                System.out.println("Enter choice: ");
                line = reader.readLine();

                switch (line) {
                    case "k": // Move up
                        currentTask = moveUp(currentTask, tree);
                        break;
                    case "j": // Move down
                        currentTask = moveDown(currentTask, tree);
                        break;
                    case "\r": // Enter key
                        //selectTask();
                        break;
                    case "cs":
                        System.out.println("Enter sub name: ");
                        String name = reader.readLine();
                        System.out.println("Enter description: ");
                        String desc = reader.readLine();
                        createSubTask(links, fullMap, currentTask, name, desc);
                        break;
                    case "cd":
                        System.out.println("Enter dep name: ");
                        String dname = reader.readLine();
                        System.out.println("Enter description: ");
                        String ddesc = reader.readLine();
                        createDepTask(links, fullMap, currentTask, dname, ddesc);
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

        writeJson("tasks.json", toJson(tasks));
        writeJson("links.json", toJson(links));
    }

    private static NodeTask moveUp(NodeTask currentTask, Map<Long, NodeTask> taskMap) {
        if (currentTask == null) {
            return null;
        }
        List<Long> all = new ArrayList<>(currentTask.getSubTasks());
        all.addAll(currentTask.getDependencies());
        int ind = all.indexOf(currentTask.getId());
        if (ind == -1 || ind == 0) {
            //Must be the parent we need then if not any other task
            return taskMap.get(currentTask.getParentId());
        }
        return taskMap.get(all.get(ind - 1));
    }

    private static NodeTask moveDown(NodeTask currentTask, Map<Long, NodeTask> taskMap) {
        if (currentTask == null) {
            return null;
        }
        List<Long> all = new ArrayList<>(currentTask.getSubTasks());
        all.addAll(currentTask.getDependencies());
        int ind = all.indexOf(currentTask.getId());
        if (ind < all.size() - 1) {
            return taskMap.get(all.get(ind + 1));
        }
        return currentTask;
    }

    private static Task createCommonTask(List<Link> links, Map<Long, Task> fullMap, NodeTask currentTask, String name, String desc) {
        long nKey = Collections.max(fullMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        NodeTask t = new NodeTask(task.getId());
        fullMap.put(task.getId(), task);
        t.setParentId(currentTask.getId());
        Link link = new Link(currentTask.getId(), LinkType.PARENT, task.getId());
        links.add(link);
        return task;
    }

    private static void createSubTask(List<Link> links, Map<Long, Task> fullMap, NodeTask currentTask, String name, String desc) {
        Task task = createCommonTask(links, fullMap, currentTask, name, desc);
        currentTask.getSubTasks().add(task.getId());
        Link link = new Link(currentTask.getId(), LinkType.SUBTASK, task.getId());
        links.add(link);
    }

    private static void createDepTask(List<Link> links, Map<Long, Task> fullMap, NodeTask currentTask, String name, String desc) {
        Task task = createCommonTask(links, fullMap, currentTask, name, desc);
        currentTask.getDependencies().add(task.getId());
        Link link = new Link(currentTask.getId(), LinkType.DEPENDENCY, task.getId());
        links.add(link);
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
            List<Task> loadedTasks = mapper.readValue(jsonFile, new TypeReference<List<Task>>() {
            });

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
            List<Link> loadedTasks = mapper.readValue(jsonFile, new TypeReference<List<Link>>() {
            });

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