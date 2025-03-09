package org.dksd.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Collection {

    private final List<Instance> instances = new ArrayList<>(); // save these
    private final ObjectMapper mapper = new ObjectMapper();
    private NodeTask curr;
    private final Map<UUID, UUID> nextPrevMap = new HashMap<>();

    public Collection(Instance... instances) {
        this.instances.addAll(Arrays.stream(instances).toList());
        curr = this.instances.getFirst().getTaskNodes().getFirst();
    }

    public void setCurrentTaskToParent() {
        if (curr.getParentId() != null) {
            curr = getInstance().getTaskNodes().get(curr.getParentId());
        }
    }

    public Instance getInstance() {
        for (Instance instance : instances) {
            if (instance.getTaskNodes().get(curr.getId()) != null) {
                return instance;
            }
        }
        return null;
    }

    public void displayTasks() {
        String greenCheck = "\u001B[32m\u2713\u001B[0m";
        //Needs to be recursive right?
        //for (Task wt : workingSet) {

        Task wt = getInstance().getTask(curr.getId());
        String suffix = curr.getId() == wt.getId() ? " (*) " : "";

        NodeTask p = getInstance().getTaskNode(wt.getId());
        List<String> hierarchy = new ArrayList<>();
        while (p.getParentId() != null) {
            p = getInstance().getTaskNode(p.getParentId());
            hierarchy.add(getInstance().getTask(p.getId()).getName());
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
                System.out.print(getInstance().getConstraint(constraint).toCompactString());
            }
            System.out.println(indent + "- " + getInstance().getTask(subTask).getName());
        }
        System.out.flush();
        if (!p.getDependencies().isEmpty()) {
            System.out.print(indent + "  Dependencies: ");
        }
        for (UUID dep : p.getDependencies()) {
            System.out.print(getInstance().getTask(dep).getName() + ", ");
        }
        System.out.flush();
    }

    public String toJson(List<?> tasks) {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void find(String searchTerm) {
        for (Task task : getInstance().getTasks()) {
            if (task.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                curr = getInstance().getTaskNode(task.getId());
            }
        }
    }

    public NodeTask setCurrentTaskToNext() {
        NodeTask prev = null;
        for (Instance instance : instances) {
            for (Map.Entry<UUID, NodeTask> entry : instance.getTaskNodes().getTaskNodeMap().entrySet()) {
                if (prev != null) {
                    nextPrevMap.put(prev.getId(), entry.getValue().getId());
                }
                prev = entry.getValue();
            }
        }
        return getInstance().getTaskNode(nextPrevMap.get(curr.getId()));
    }

    public Task getCurrentTask() {
        return getInstance().getTask(curr.getId());
    }
}
