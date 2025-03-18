package org.dksd.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Collection {

    private final List<Instance> instances = new ArrayList<>(); // save these
    private final ObjectMapper mapper = new ObjectMapper();
    private NodeTask curr;

    public Collection(Instance... instances) {
        this.instances.addAll(Arrays.stream(instances).toList());
        mapper.registerModule(new JavaTimeModule());
    }

    public Instance getInstance() {
        for (Instance instance : instances) {
            if (getCurrentNodeTask() == null || instance.getTaskNodes().get(getCurrentNodeTask().getId()) != null) {
                return instance;
            }
        }
        return null;
    }

    private void deleteTask(Task currentTask) {
        getInstance().removeTask(currentTask);
    }

    public void displayTasks(List<NodeTask> path, TreeMap<Double, Integer> sorted) {
        //String greenCheck = "\u001B[32m\u2713\u001B[0m";
        //TODO

        for (NodeTask nodeTask : path) {
            String suffix = (nodeTask.equals(curr)) ? "(*)" : "";
            if (!getInstance().isParent(nodeTask.getId())) {
                System.out.println(getInstance().getTask(nodeTask.getId()).getName() + " <- " + getInstance().getHierarchy(nodeTask) + " " + suffix);
            }
        }
        /*List<String> hierarchy = new ArrayList<>();
        while (wtn.getParentId() != null) {
            wtn = getInstance().getTaskNode(wtn.getParentId());
            hierarchy.add(getInstance().getTask(wtn.getId()).getName());
        }

        wtn = getInstance().getTaskNode(wt.getId());
        String indent = "  ";
        System.out.println(wt.getName() + " <- " + hierarchy);
        //System.out.println(suffix + "   Description: " + wt.getDescription());
        System.out.flush();

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
        */
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

    public void dfs(List<NodeTask> path, NodeTask nt) {
        if (nt == null)
            return;

        path.add(nt);

        for (UUID subTask : nt.getSubTasks()) {
            dfs(path, getInstance().getTaskNode(subTask));
        }
        for (UUID depTask : nt.getDependencies()) {
            dfs(path, getInstance().getTaskNode(depTask));
        }
    }

    public List<NodeTask> getInlineTasks() {
        List<NodeTask> path = new ArrayList<>();
        dfs(path, getRootTask(curr));
        return path;
    }

    public NodeTask setCurrentTask(List<NodeTask> path, Function<Integer, Integer> indexSelector) {
        System.out.println("Current node task: " + curr);

        // Find the index of the current task in the path.
        int currentIndex = IntStream.range(0, path.size())
                .filter(i -> path.get(i).getId().equals(curr.getId()))
                .findFirst()
                .orElse(-1);

        if (currentIndex == -1) {
            // If current task is not found, return the current task.
            return curr;
        }

        // Calculate new index based on provided function.
        int newIndex = indexSelector.apply(currentIndex);
        while (getInstance().isParent(path.get(newIndex).getId())) {
            newIndex = indexSelector.apply(newIndex);
        }
        curr = path.get(newIndex);

        System.out.println("Updated node task: " + curr);
        return curr;
    }

    private NodeTask getRootTask(NodeTask nt) {
        if (nt.getParentId() == null) {
            return nt;
        }
        return getRootTask(getInstance().getTaskNode(nt.getParentId()));
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

    public int getTotalTaskCount() {
        int tot = 0;
        for (Instance instance : instances) {
            tot += instance.getTasks().size();
        }
        return tot;
    }
}
