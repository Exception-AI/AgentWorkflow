package org.dksd.tasks;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;

public class Helper {

    private final Instance instance;
    private List<Task> workingSet = new ArrayList<>();
    private NodeTask currentTask = null;

    public Helper(Instance instance) {
        this.instance = instance;
        workingSet = new ArrayList<>();
        selectTasks();
        currentTask = (!workingSet.isEmpty()) ? instance.getTaskNodeMap().get(workingSet.stream().findFirst().get().getId()) : null;
    }

    private List<UUID> getParentSubTasks(NodeTask node) {
        return instance.getTaskNodeMap().get(node.getParentId()).getSubTasks();
    }

    private List<UUID> getParentDepTasks(NodeTask node) {
        return instance.getTaskNodeMap().get(node.getParentId()).getDependencies();
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
        if (instance.getTaskMap().isEmpty()) {
            return;
        }
        Random rand = new Random();
        List<UUID> indexes = new ArrayList<>(instance.getTaskMap().keySet());
        for (int i = 0; i < 10 && workingSet.size() < 10; i++) {
            int ii = rand.nextInt(indexes.size());
            if (!workingSet.contains(instance.getTaskMap().get(indexes.get(ii)))) {
                workingSet.add(instance.getTaskMap().get(indexes.get(ii)));
            }
        }
    }

    public void displayTasks() {
        String greenCheck = "\u001B[32m\u2713\u001B[0m";
        //Needs to be recursive right?
        //for (Task wt : workingSet) {
        Task wt = instance.getTaskMap().get(currentTask.getId());
        String suffix = currentTask != null && currentTask.getId() == wt.getId() ? " (*) " : "";

        NodeTask p = instance.getTaskNodeMap().get(wt.getId());
        List<String> hierarchy = new ArrayList<>();
        while (p.getParentId() != null) {
            p = instance.getTaskNodeMap().get(p.getParentId());
            hierarchy.add(instance.getTaskMap().get(p.getId()).getName());
        }

        String indent = "  ";
        System.out.println(wt.getName() + " <- " + hierarchy + " " + suffix);
        //System.out.println(suffix + "   Description: " + wt.getDescription());
        System.out.flush();
            /*if (!taskNodeMap.get(wt.getId()).getSubTasks().isEmpty()) {
                System.out.println(suffix + "   SubTasks: ");
            }*/

        for (UUID subTask : p.getSubTasks()) {
            for (UUID constraint : p.getConstraints()) {
                System.out.print(instance.getConstraintMap().get(constraint).toCompactString());
            }
            System.out.println(indent + "- " + instance.getTaskMap().get(subTask).getName());
        }
        System.out.flush();
        if (!p.getDependencies().isEmpty()) {
            System.out.print(indent + "  Dependencies: ");
        }
        for (UUID dep : p.getDependencies()) {
            System.out.print(instance.getTaskMap().get(dep).getName() + ", ");
        }

        System.out.flush();
        //}
    }

    public NodeTask getCurrentTask() {
        return currentTask;
    }

    public Task getCurrent() {
        return instance.getTaskMap().get(currentTask.getId());
    }

    public List<Task> getWorkingSet() {
        return workingSet;
    }

    public void setCurrentTask(NodeTask nodeTask) {
        this.currentTask = nodeTask;
    }

    public NodeTask nextTask(NodeTask currentTask) {
        Map.Entry<UUID, NodeTask> nextEntry = instance.getTaskNodeMap().higherEntry(currentTask.getId());

        if (nextEntry != null) {
            return nextEntry.getValue();
        } else {
            return currentTask; // or handle the case differently
        }
    }

    public NodeTask prevTask(NodeTask currentTask) {
        Map.Entry<UUID, NodeTask> nextEntry = instance.getTaskNodeMap().lowerEntry(currentTask.getId());

        if (nextEntry != null) {
            return nextEntry.getValue();
        } else {
            return currentTask; // or handle the case differently
        }
    }

    public void setCurrentTaskToParent() {
        if (currentTask.getParentId() != null) {
            currentTask = instance.getTaskNodeMap().get(currentTask.getParentId());
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

    public void find(String searchTerm) {
        for (Task task : instance.getTasks()) {
            if (task.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                currentTask = instance.getTaskNodeMap().get(task.getId());
            }
        }
    }

    public List<Task> getTasks() {
        return instance.getTasks();
    }

    public List<Link> getLinks() {
        return instance.getLinks();
    }

    public List<Constraint> getConstraints() {
        return instance.getConstraints();
    }

    public void createProjectTask(String name, String desc) {
        instance.createProjectTask(name, desc);
    }

    public Instance getInstance() {
        return instance;
    }
}
