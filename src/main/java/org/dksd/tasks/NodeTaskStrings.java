package org.dksd.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NodeTaskStrings {

    private final String parentName;
    private final String name;
    private final List<String> subTasks = new ArrayList<>();
    private final List<String> dependencies = new ArrayList<>();
    private final List<String> constraints = new ArrayList<>();

    public NodeTaskStrings(Instance inst, NodeTask nt) {
        if (nt.getParentId() != null) {
            parentName = inst.getTask(nt.getParentId()).getName();
        } else {
            parentName = null;
        }
        name = inst.getTask(nt.getId()).getName();
        for (UUID subTask : nt.getSubTasks()) {
            subTasks.add(inst.getTask(subTask).getName());
        }
        for (UUID dep : nt.getDependencies()) {
            dependencies.add(inst.getTask(dep).getName());
        }
        for (UUID c : nt.getConstraints()) {
            constraints.add(inst.getConstraint(c).getName());
        }
    }


    public List<String> getSubTasks() {
        return subTasks;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<String> getConstraints() {
        return constraints;
    }


    @Override
    public String toString() {
        return "Task{" +
                "parentName='" + parentName + '\'' +
                ", name='" + name + '\'' +
                ", subTasks=" + subTasks +
                ", dependencies=" + dependencies +
                ", constraints=" + constraints +
                '}';
    }
}
