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
    }

    public void setCurrentTaskToParent() {
        if (getCurrentNodeTask() != null && getCurrentNodeTask().getParentId() != null) {
            curr = getInstance().getTaskNodes().get(getCurrentNodeTask().getParentId());
        }
    }

    public Instance getInstance() {
        for (Instance instance : instances) {
            if (getCurrentNodeTask() == null || instance.getTaskNodes().get(getCurrentNodeTask().getId()) != null) {
                return instance;
            }
        }
        return null;
    }

    public void displayTasks() {
        String greenCheck = "\u001B[32m\u2713\u001B[0m";
        //Needs to be recursive right?
        //for (Task wt : workingSet) {

        Task wt = getCurrentTask();

        NodeTask wtn = getInstance().getTaskNode(wt.getId());
        List<String> hierarchy = new ArrayList<>();
        while (wtn.getParentId() != null) {
            wtn = getInstance().getTaskNode(wtn.getParentId());
            hierarchy.add(getInstance().getTask(wtn.getId()).getName());
        }

        String indent = "  ";
        System.out.println(wt.getName() + " <- " + hierarchy);
        //System.out.println(suffix + "   Description: " + wt.getDescription());
        System.out.flush();
            /*if (!taskNodeMap.get(wt.getId()).getSubTasks().isEmpty()) {
                System.out.println(suffix + "   SubTasks: ");
            }*/

        for (UUID subTask : wtn.getSubTasks()) {
            for (UUID constraint : getInstance().getTaskNode(subTask).getConstraints()) {
                System.out.print(getInstance().getConstraint(constraint).toCompactString());
            }
            System.out.println(indent + "- " + getInstance().getTask(subTask).getName());
        }
        System.out.flush();
        if (!wtn.getDependencies().isEmpty()) {
            System.out.print(indent + "  Dependencies: ");
        }
        for (UUID dep : wtn.getDependencies()) {
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
                if (prev != null && prev.getId().equals(getCurrentNodeTask().getId())) {
                    return entry.getValue();
                }
                if (!entry.getValue().getId().equals(getCurrentNodeTask().getId())) {
                    prev = entry.getValue();
                }
            }
        }
        curr = prev;
        return prev;
    }

    public Task getCurrentTask() {
        if (getCurrentNodeTask() == null) {
            return null;
        }
        return getInstance().getTask(getCurrentNodeTask().getId());
    }

    private NodeTask getCurrentNodeTask() {
        if (curr != null) {
            return curr;
        }
        if (!this.instances.getFirst().getTasks().isEmpty()) {
            curr = this.instances.getFirst().getTaskNodes().getFirst();
        }
        return null;
    }
}
