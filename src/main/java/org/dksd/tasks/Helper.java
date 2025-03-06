package org.dksd.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jline.reader.LineReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class Helper {

    private Map<Long, Task> taskMap = new HashMap<>();
    private List<Task> workingSet = new ArrayList<>();
    private List<Task> tasks = null;
    private List<Link> links = null;
    private List<Constraint> constraints = null;
    private TreeMap<Long, NodeTask> taskNodeMap = new TreeMap<>();
    private NodeTask currentTask = null;
    private Task ROOT = new Task(0L, "ROOT", "ROOT");

    public Helper(File taskFile, File linksFile, File constraintsFile) {
        load(taskFile, linksFile, constraintsFile);
    }

    public Helper(String taskFilename, String linksFilename, String constraintsFilename) {
        load(new File(taskFilename), new File(linksFilename), new File(constraintsFilename));
    }

    private void load(File taskFile, File linksFile, File constraintsFile) {
        taskMap = new HashMap<>();
        workingSet = new ArrayList<>();
        tasks = loadTasks(taskFile);
        links = loadLinks(linksFile);
        constraints = loadConstraints(constraintsFile);
        taskMap.put(ROOT.getId(), ROOT);
        taskNodeMap.put(ROOT.getId(), new NodeTask(0));
        tasks.forEach(task -> taskMap.put(task.getId(), task));
        buildTree();
        selectTasks();
        currentTask = (!workingSet.isEmpty()) ? taskNodeMap.get(workingSet.stream().findFirst().get().getId()) : null;
    }

    public NodeTask moveUp(NodeTask current) {
        if (current.getId() == ROOT.getId()) {
            return current;
        }
        if (!isFirst(getParentSubTasks(current), current.getId()) && isPresent(getParentSubTasks(current), current.getId())) {
            return taskNodeMap.get(prev(getParentSubTasks(current), current.getId()));
        }
        return taskNodeMap.get(current.getParentId());
    }

    private Long prev(List<Long> list, Long indx) {
        int index = list.indexOf(indx);
        if (index < 1) {
            return -1L;
        }
        return list.get(index - 1);
    }

    private Long nextInList(List<Long> list, Long indx) {
        int index = list.indexOf(indx);
        if (index == -1 || index >= list.size() - 1) {
            return -1L;
        }
        return list.get(index + 1);
    }

    private List<Long> getParentSubTasks(NodeTask node) {
        return taskNodeMap.get(node.getParentId()).getSubTasks();
    }

    private List<Long> getParentDepTasks(NodeTask node) {
        return taskNodeMap.get(node.getParentId()).getDependencies();
    }

    private boolean isSubTask(NodeTask node) {
        return getParentSubTasks(node).contains(node.getId());
    }

    private boolean isDepTask(NodeTask node) {
        return getParentDepTasks(node).contains(node.getId());
    }

    public NodeTask moveDown(NodeTask current) {
        if (!isLast(getParentSubTasks(current), current.getId()) && isPresent(getParentSubTasks(current), current.getId())) {
            return taskNodeMap.get(nextInList(getParentSubTasks(current), current.getId()));
        }
        return current;
    }

    private boolean isLast(List<Long> parentSubTasks, Long nodeId) {
        if (parentSubTasks == null || parentSubTasks.isEmpty()) {
            return false;
        }
        return parentSubTasks.getLast().equals(nodeId);
    }

    private boolean isFirst(List<Long> parentSubTasks, Long nodeId) {
        if (parentSubTasks == null || parentSubTasks.isEmpty()) {
            return false;
        }
        return parentSubTasks.getFirst().equals(nodeId);
    }

    private boolean isPresent(List<Long> parentSubTasks, Long nodeId) {
        return parentSubTasks.contains(nodeId);
    }

    public Task createCommonTask(Task parent, String name, String desc) {
        assert parent != null;
        long nKey = (taskMap.isEmpty()) ? 1 : Collections.max(taskMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        tasks.add(task);
        NodeTask t = new NodeTask(task.getId());
        addTaskToTree(task);
        taskMap.put(task.getId(), task);
        t.setParentId(parent.getId());
        Link link = new Link(parent.getId(), LinkType.PARENT, task.getId());
        links.add(link);
        addLinkToTree(link);
        return task;
    }

    public Task createProjectTask(String name, String desc) {
        return createSubTask(ROOT, name, desc);
    }

    public Task createSubTask(Task parent, String name, String desc) {
        Task task = createCommonTask(parent, name, desc);
        Link link = new Link(parent.getId(), LinkType.SUBTASK, task.getId());
        links.add(link);
        addLinkToTree(link);
        return task;
    }

    public Task createDepTask(Task parent, String name, String desc) {
        Task task = createCommonTask(parent, name, desc);
        Link link = new Link(parent.getId(), LinkType.DEPENDENCY, task.getId());
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
            if (!workingSet.contains(taskMap.get(indexes.get(ii)))) {
                workingSet.add(taskMap.get(indexes.get(ii)));
            }
        }
    }

    public void displayTasks() {
        for (Task wt : workingSet) {

            String suffix = currentTask != null && currentTask.getId() == wt.getId() ? " (*) " : "";

            NodeTask p = taskNodeMap.get(wt.getId());
            String hierarchy = "";
            while (p.getParentId() != null) {
                p = taskNodeMap.get(p.getParentId());
                hierarchy = taskMap.get(p.getId()).getName() + " -> " + hierarchy;
            }

            System.out.println(suffix + " Task: " + hierarchy);
            System.out.println(suffix + "   Name       : " + wt.getName());
            System.out.println(suffix + "   Description: " + wt.getDescription());
            System.out.flush();
            if (!taskNodeMap.get(wt.getId()).getSubTasks().isEmpty()) {
                System.out.println(suffix + "   SubTasks: ");
            }
            for (Long subTask : taskNodeMap.get(wt.getId()).getSubTasks()) {
                suffix = currentTask != null && currentTask.getId() == subTask ? " (*) " : "";
                System.out.println("      - " + taskMap.get(subTask).getName() + suffix + " : " + taskMap.get(subTask).getDescription());
            }
            System.out.flush();
            if (!taskNodeMap.get(wt.getId()).getDependencies().isEmpty()) {
                System.out.println(suffix + "  Dependencies: ");
            }
            for (Long dep : taskNodeMap.get(wt.getId()).getDependencies()) {
                suffix = currentTask != null && currentTask.getId() == dep ? " (*) " : "";
                System.out.println("      - " + taskMap.get(dep).getName() + suffix + " : " + taskMap.get(dep).getDescription());
            }
            System.out.flush();
            System.out.println();
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

    public Task getCurrent() {
        return taskMap.get(currentTask.getId());
    }

    public List<Task> getWorkingSet() {
        return workingSet;
    }

    public void setCurrentTask(NodeTask nodeTask) {
        this.currentTask = nodeTask;
    }

    public NodeTask nextTask(NodeTask currentTask) {
        Map.Entry<Long, NodeTask> nextEntry = taskNodeMap.higherEntry(currentTask.getId());

        if (nextEntry != null) {
            return nextEntry.getValue();
        } else {
            return currentTask; // or handle the case differently
        }
    }

    public NodeTask prevTask(NodeTask currentTask) {
        Map.Entry<Long, NodeTask> nextEntry = taskNodeMap.lowerEntry(currentTask.getId());

        if (nextEntry != null) {
            return nextEntry.getValue();
        } else {
            return currentTask; // or handle the case differently
        }
    }

    public void setCurrentTaskToParent() {
        currentTask = taskNodeMap.get(currentTask.getParentId());
    }

    public void multiInput(BufferedReader reader, BiConsumer<String, String> updateFunction) throws IOException {
        System.out.print("Edit name: ");
        String name = reader.readLine();
        System.out.print("Edit description: ");
        String desc = reader.readLine();

        updateFunction.accept(name, desc);
    }

    public void updateTask(Task current, String ename, String edesc) {
        current.setName(ename);
        current.setDescription(edesc);
    }
}
