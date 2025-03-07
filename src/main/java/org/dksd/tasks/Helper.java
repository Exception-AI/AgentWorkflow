package org.dksd.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dksd.tasks.model.LinkType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiConsumer;

public class Helper {

    private Instance instance;
    private Map<UUID, Task> taskMap = new HashMap<>();
    private List<Task> workingSet = new ArrayList<>();
    private List<Task> tasks = null;
    private List<Link> links = null;
    private List<Constraint> constraints = null;
    private final Map<UUID, Constraint> constraintMap = new HashMap<>();
    private final TreeMap<UUID, NodeTask> taskNodeMap = new TreeMap<>();
    private NodeTask currentTask = null;
    private final Task ROOT = new Task(UUID.nameUUIDFromBytes("0L".getBytes()), "ROOT", "ROOT");
    private final ObjectMapper mapper = new ObjectMapper();

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
        if (constraints.isEmpty()) {
            for (Task task : tasks) {
                Constraint constraint = new Constraint();
                constraint.defaultConfig();
                constraint.setTaskId(task.getId());
                constraint.setConstraintId(task.getId());
                constraints.add(constraint);
                constraintMap.put(constraint.getConstraintId(), constraint);
            }
        }
        taskMap.put(ROOT.getId(), ROOT);
        taskNodeMap.put(ROOT.getId(), new NodeTask(ROOT.getId()));
        for (Task task : tasks) {
            taskMap.put(task.getId(), task);
        }
        buildTree();
        selectTasks();
        currentTask = (!workingSet.isEmpty()) ? taskNodeMap.get(workingSet.stream().findFirst().get().getId()) : null;
    }

    private List<UUID> getParentSubTasks(NodeTask node) {
        return taskNodeMap.get(node.getParentId()).getSubTasks();
    }

    private List<UUID> getParentDepTasks(NodeTask node) {
        return taskNodeMap.get(node.getParentId()).getDependencies();
    }

    private boolean isSubTask(NodeTask node) {
        return getParentSubTasks(node).contains(node.getId());
    }

    private boolean isDepTask(NodeTask node) {
        return getParentDepTasks(node).contains(node.getId());
    }

    private boolean isLast(List<UUID> parentSubTasks, UUID nodeId) {
        if (parentSubTasks == null || parentSubTasks.isEmpty()) {
            return false;
        }
        return parentSubTasks.getLast().equals(nodeId);
    }

    private boolean isFirst(List<UUID> parentSubTasks, UUID nodeId) {
        if (parentSubTasks == null || parentSubTasks.isEmpty()) {
            return false;
        }
        return parentSubTasks.getFirst().equals(nodeId);
    }

    private boolean isPresent(List<UUID> parentSubTasks, UUID nodeId) {
        return parentSubTasks.contains(nodeId);
    }

    public Task createCommonTask(Task parent, String name, String desc) {
        assert parent != null;
        UUID nKey = UUID.randomUUID();//(taskMap.isEmpty()) ? 1 : Collections.max(taskMap.keySet()) + 1;
        UUID cKey = UUID.randomUUID();//(constraintMap.isEmpty()) ? 1 : Collections.max(constraintMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        tasks.add(task);
        taskMap.put(task.getId(), task);
        NodeTask t = new NodeTask(task.getId());
        taskNodeMap.put(task.getId(), t);
        t.setParentId(parent.getId());
        Link link = new Link(parent.getId(), LinkType.PARENT, task.getId());
        links.add(link);
        addLinkToTree(link);
        Constraint constraint = new Constraint();
        constraint.setTaskId(t.getId());
        constraint.setConstraintId(cKey);
        constraint.defaultConfig();
        constraints.add(constraint);
        constraintMap.put(constraint.getConstraintId(), constraint);
        t.getConstraints().add(task.getId());
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
            NodeTask t = new NodeTask(task.getId());
            taskNodeMap.put(task.getId(), t);
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
        try {
            return mapper.readValue(file, new TypeReference<List<Task>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Link> loadLinks(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Link>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Constraint> loadConstraints(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Constraint>>() {
            });
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
        List<UUID> indexes = new ArrayList<>(taskMap.keySet());
        for (int i = 0; i < 10 && workingSet.size() < 10; i++) {
            int ii = rand.nextInt(indexes.size());
            if (!workingSet.contains(taskMap.get(indexes.get(ii)))) {
                workingSet.add(taskMap.get(indexes.get(ii)));
            }
        }
    }

    public void displayTasks() {
        String greenCheck = "\u001B[32m\u2713\u001B[0m";
        //Needs to be recursive right?
        //for (Task wt : workingSet) {
        Task wt = taskMap.get(currentTask.getId());
        String suffix = currentTask != null && currentTask.getId() == wt.getId() ? " (*) " : "";

        NodeTask p = taskNodeMap.get(wt.getId());
        List<String> hierarchy = new ArrayList<>();
        while (p.getParentId() != null) {
            p = taskNodeMap.get(p.getParentId());
            hierarchy.add(taskMap.get(p.getId()).getName());
        }

        String indent = "  ";
        System.out.println(wt.getName() + " <- " + hierarchy + " " + suffix);
        //System.out.println(suffix + "   Description: " + wt.getDescription());
        System.out.flush();
            /*if (!taskNodeMap.get(wt.getId()).getSubTasks().isEmpty()) {
                System.out.println(suffix + "   SubTasks: ");
            }*/

        for (UUID subTask : taskNodeMap.get(wt.getId()).getSubTasks()) {
            for (Constraint constraint : constraints) {
                if (constraint.getTaskId() == subTask) {
                    System.out.print(constraint.toCompactString());
                    break;
                }
            }
            System.out.println(indent + "- " + taskMap.get(subTask).getName());
        }
        System.out.flush();
        if (!taskNodeMap.get(wt.getId()).getDependencies().isEmpty()) {
            System.out.print(indent + "  Dependencies: ");
        }
        for (UUID dep : taskNodeMap.get(wt.getId()).getDependencies()) {
            System.out.print(taskMap.get(dep).getName() + ", ");
        }

        System.out.flush();
        //}
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Map<UUID, NodeTask> getTaskNodeMap() {
        return taskNodeMap;
    }

    public Map<UUID, Task> getTaskMap() {
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
        Map.Entry<UUID, NodeTask> nextEntry = taskNodeMap.higherEntry(currentTask.getId());

        if (nextEntry != null) {
            return nextEntry.getValue();
        } else {
            return currentTask; // or handle the case differently
        }
    }

    public NodeTask prevTask(NodeTask currentTask) {
        Map.Entry<UUID, NodeTask> nextEntry = taskNodeMap.lowerEntry(currentTask.getId());

        if (nextEntry != null) {
            return nextEntry.getValue();
        } else {
            return currentTask; // or handle the case differently
        }
    }

    public void setCurrentTaskToParent() {
        if (currentTask.getParentId() != null) {
            currentTask = taskNodeMap.get(currentTask.getParentId());
        }
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

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void find(String searchTerm) {
        for (Task task : tasks) {
            if (task.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                currentTask = taskNodeMap.get(task.getId());
            }
        }
    }
}
