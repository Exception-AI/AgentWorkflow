package org.dksd.tasks;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class Helper {

    private final Instance instance;
    private NodeTask currentTask = null;

    public Helper(Instance instance) {
        this.instance = instance;
        currentTask = instance.getRoot();
    }

    private List<UUID> getParentSubTasks(NodeTask node) {
        return instance.getTaskNode(node.getParentId()).getSubTasks();
    }

    private List<UUID> getParentDepTasks(NodeTask node) {
        return instance.getTaskNode(node.getParentId()).getDependencies();
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

    public void displayTasks() {
        String greenCheck = "\u001B[32m\u2713\u001B[0m";
        //Needs to be recursive right?
        //for (Task wt : workingSet) {
        Task wt = instance.getTask(currentTask.getId());
        String suffix = currentTask != null && currentTask.getId() == wt.getId() ? " (*) " : "";

        NodeTask p = instance.getTaskNode(wt.getId());
        List<String> hierarchy = new ArrayList<>();
        while (p.getParentId() != null) {
            p = instance.getTaskNode(p.getParentId());
            hierarchy.add(instance.getTask(p.getId()).getName());
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
                System.out.print(instance.getConstraint(constraint).toCompactString());
            }
            System.out.println(indent + "- " + instance.getTask(subTask).getName());
        }
        System.out.flush();
        if (!p.getDependencies().isEmpty()) {
            System.out.print(indent + "  Dependencies: ");
        }
        for (UUID dep : p.getDependencies()) {
            System.out.print(instance.getTask(dep).getName() + ", ");
        }

        System.out.flush();
        //}
    }

    public NodeTask getCurrentTask() {
        return currentTask;
    }

    public Task getCurrent() {
        return instance.getTask(currentTask.getId());
    }

    public void setCurrentTask(NodeTask nodeTask) {
        this.currentTask = nodeTask;
    }

    public void setCurrentTaskToParent() {
        if (currentTask.getParentId() != null) {
            currentTask = instance.getTaskNode(currentTask.getParentId());
        }
    }

    public NodeTask setCurrentTaskToNext(UUID id) {
        if (instance.getTaskNode(id).getSubTasks().contains(id)) {
            int indx = instance.getTaskNode(id).getSubTasks().indexOf(id);
            if (indx < instance.getTaskNode(id).getSubTasks().size() - 1) {
                return instance.getTaskNode(instance.getTaskNode(id).getSubTasks().get(indx + 1));
            }
        }
        if (instance.getTaskNode(id).getDependencies().contains(id)) {
            int indx = instance.getTaskNode(id).getDependencies().indexOf(id);
            if (indx < instance.getTaskNode(id).getDependencies().size() - 1) {
                return instance.getTaskNode(instance.getTaskNode(id).getDependencies().get(indx + 1));
            }
        }
        if (instance.getTaskNode(id).getParentId() != null) {
            setCurrentTaskToNext(instance.getTaskNode(id).getParentId());
        }
        return currentTask;
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
                currentTask = instance.getTaskNode(task.getId());
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
