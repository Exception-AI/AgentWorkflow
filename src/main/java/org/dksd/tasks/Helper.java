package org.dksd.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Helper {

    private Map<Long, Task> fullMap = new HashMap<>();
    private List<Task> workingSet = new ArrayList<>();
    private List<Task> tasks = null;
    private List<Link> links = null;
    private Map<Long, NodeTask> tree = null;
    private NodeTask currentTask = null;

    public Helper(File taskFile, File linksFile) {
        load(taskFile, linksFile);
    }

    public Helper(String taskFilename, String linksFilename) {
        load(new File(taskFilename), new File(linksFilename));
    }

    private void load(File taskFile, File linksFile) {
        fullMap = new HashMap<>();
        workingSet = new ArrayList<>();
        tasks = loadTasks(taskFile);
        links = loadLinks(linksFile);
        tree = buildTree(tasks, links);
        tasks.forEach(task -> {
            fullMap.put(task.getId(), task);
        });
        selectTasks();
        currentTask = (!workingSet.isEmpty()) ? tree.get(workingSet.stream().findFirst().get().getId()) : null;
    }

    public NodeTask moveUp() {
        if (currentTask == null) {
            return null;
        }
        List<Long> all = new ArrayList<>(currentTask.getSubTasks());
        all.addAll(currentTask.getDependencies());
        int ind = all.indexOf(currentTask.getId());
        if (ind == -1 || ind == 0) {
            //Must be the parent we need then if not any other task
            currentTask = tree.get(currentTask.getParentId());
        } else {
            currentTask = tree.get(all.get(ind - 1));
        }
        return currentTask;
    }

    public NodeTask moveDown() {
        if (currentTask == null) {
            return null;
        }
        List<Long> all = new ArrayList<>(currentTask.getSubTasks());
        all.addAll(currentTask.getDependencies());
        int ind = all.indexOf(currentTask.getId());
        if (ind < all.size() - 1) {
            currentTask = tree.get(all.get(ind + 1));
        }
        return currentTask;
    }

    public Task createCommonTask(List<Link> links, Map<Long, Task> fullMap, NodeTask currentTask, String name, String desc) {
        long nKey = Collections.max(fullMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        NodeTask t = new NodeTask(task.getId());
        fullMap.put(task.getId(), task);
        t.setParentId(currentTask.getId());
        Link link = new Link(currentTask.getId(), LinkType.PARENT, task.getId());
        links.add(link);
        return task;
    }

    public void createSubTask(String name, String desc) {
        Task task = createCommonTask(links, fullMap, currentTask, name, desc);
        currentTask.getSubTasks().add(task.getId());
        Link link = new Link(currentTask.getId(), LinkType.SUBTASK, task.getId());
        links.add(link);
    }

    public void createDepTask(String name, String desc) {
        Task task = createCommonTask(links, fullMap, currentTask, name, desc);
        currentTask.getDependencies().add(task.getId());
        Link link = new Link(currentTask.getId(), LinkType.DEPENDENCY, task.getId());
        links.add(link);
    }

    public Map<Long, NodeTask> buildTree(List<Task> tasks, List<Link> links) {
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

    public String toJson(List<?> tasks) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Task> loadTasks(File file) {
        // Create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();

        try {

            // Deserialize JSON into a Task object.
            List<Task> loadedTasks = mapper.readValue(file, new TypeReference<List<Task>>() {
            });

            System.out.println("Loaded tasks");
            return loadedTasks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Link> loadLinks(File file) {
        // Create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Deserialize JSON into a Task object.
            List<Link> loadedTasks = mapper.readValue(file, new TypeReference<List<Link>>() {
            });

            System.out.println("Loaded tasks");
            return loadedTasks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void writeJson(String fileName, String json) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
            fileWriter.flush();
            System.out.println("Successfully saved JSON to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void selectTasks() {
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

    public void displayTasks() {
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

    public List<?> getTasks() {
        return tasks;
    }

    public List<?> getLinks() {
        return links;
    }
}
