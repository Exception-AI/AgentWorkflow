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

    private Map<Long, Task> taskMap = new HashMap<>();
    private List<Task> workingSet = new ArrayList<>();
    private List<Task> tasks = null;
    private List<Link> links = null;
    private Map<Long, NodeTask> taskNodeMap = new HashMap<>();
    private NodeTask currentTask = null;

    public Helper(File taskFile, File linksFile) {
        load(taskFile, linksFile);
    }

    public Helper(String taskFilename, String linksFilename) {
        load(new File(taskFilename), new File(linksFilename));
    }

    private void load(File taskFile, File linksFile) {
        taskMap = new HashMap<>();
        workingSet = new ArrayList<>();
        tasks = loadTasks(taskFile);
        links = loadLinks(linksFile);
        buildTree();
        tasks.forEach(task -> taskMap.put(task.getId(), task));
        selectTasks();
        currentTask = (!workingSet.isEmpty()) ? taskNodeMap.get(workingSet.stream().findFirst().get().getId()) : null;
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
            currentTask = taskNodeMap.get(currentTask.getParentId());
        } else {
            currentTask = taskNodeMap.get(all.get(ind - 1));
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
            currentTask = taskNodeMap.get(all.get(ind + 1));
        }
        return currentTask;
    }

    public Task createCommonTask(String name, String desc) {
        long nKey = (taskMap.isEmpty()) ? 1 : Collections.max(taskMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        tasks.add(task);
        NodeTask t = new NodeTask(task.getId());
        addTaskToTree(task);
        taskMap.put(task.getId(), task);
        if (currentTask != null) {
            t.setParentId(currentTask.getId());
            Link link = new Link(currentTask.getId(), LinkType.PARENT, task.getId());
            links.add(link);
            addLinkToTree(link);
        } else {
            currentTask = t;
        }
        return task;
    }

    public Task createTask(String name, String desc) {
        return createCommonTask(name, desc);
    }

    public Task createSubTask(String name, String desc) {
        Task task = createCommonTask(name, desc);
        currentTask.getSubTasks().add(task.getId());
        Link link = new Link(currentTask.getId(), LinkType.SUBTASK, task.getId());
        links.add(link);
        addLinkToTree(link);
        return task;
    }

    public Task createDepTask(String name, String desc) {
        Task task = createCommonTask(name, desc);
        currentTask.getDependencies().add(task.getId());
        Link link = new Link(currentTask.getId(), LinkType.DEPENDENCY, task.getId());
        links.add(link);
        addLinkToTree(link);
        return task;
    }

    private NodeTask addTaskToTree(Task task) {
        NodeTask t = new NodeTask(task.getId());
        taskNodeMap.put(task.getId(), t);
        return t;
    }

    private void addLinkToTree(Link link) {
        if (LinkType.PARENT.equals(link.getLinkType())) {
            taskNodeMap.get(link.getRight()).setParentId(link.getLeft());
        }
        if (LinkType.CHILD.equals(link.getLinkType())) {
            taskNodeMap.get(link.getLeft()).setParentId(link.getRight());
        }
        if (LinkType.SUBTASK.equals(link.getLinkType())) {
            taskNodeMap.get(link.getLeft()).getSubTasks().add(link.getRight());
        }
        if (LinkType.DEPENDENCY.equals(link.getLinkType())) {
            taskNodeMap.get(link.getLeft()).getDependencies().add(link.getRight());
        }
    }

    public void buildTree() {
        taskNodeMap.clear();
        for (Task task : tasks) {
            addTaskToTree(task);
        }
        for (Link link : links) {
            addLinkToTree(link);
        }
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
            List<Task> loadedTasks = mapper.readValue(file, new TypeReference<>() {
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
            List<Link> loadedTasks = mapper.readValue(file, new TypeReference<>() {
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
        if (taskMap.isEmpty()) {
            return;
        }
        Random rand = new Random();
        List<Long> indexes = new ArrayList<>(taskMap.keySet());
        for (int i = 0; i < 10 && workingSet.size() < 10; i++) {
            int ii = rand.nextInt(indexes.size());
            workingSet.add(taskMap.get(indexes.get(ii)));
        }
    }

    public void displayTasks() {
        for (Task wt : workingSet) {
            NodeTask p = taskNodeMap.get(wt.getId());
            while (p.getParentId() != null) {
                System.out.println(taskMap.get(wt.getId()).getName() + " -> ");
                p = taskNodeMap.get(p.getParentId());
            }

            System.out.println("Task: " + wt.getName() + " : " + wt.getDescription());

            for (Long subTask : taskNodeMap.get(wt.getId()).getSubTasks()) {
                System.out.println("  SubTasks: ");
                System.out.println("      - " + taskMap.get(subTask).getName() + " : " + taskMap.get(subTask).getDescription());
            }
            for (Long dep : taskNodeMap.get(wt.getId()).getDependencies()) {
                System.out.println("  Dependencies: ");
                System.out.println("      - " + taskMap.get(dep).getName() + " : " + taskMap.get(dep).getDescription());
            }
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Map<Long, NodeTask> getTaskNodeMap() {
        return taskNodeMap;
    }

    public Map<Long, Task> getTaskMap() {
        return taskMap;
    }

    public NodeTask getCurrentTask() {
        return currentTask;
    }

    public List<Task> getWorkingSet() {
        return workingSet;
    }
}
