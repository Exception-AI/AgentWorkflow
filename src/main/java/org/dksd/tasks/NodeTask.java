package org.dksd.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NodeTask {

    private UUID parentId;
    private final UUID id;
    private final List<UUID> subTasks = new ArrayList<>();
    private final List<UUID> dependencies = new ArrayList<>();
    private final List<UUID> constraints = new ArrayList<>();

    public NodeTask(UUID id) {
        this.id = id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getId() {
        return id;
    }

    public List<UUID> getSubTasks() {
        return subTasks;
    }

    public List<UUID> getDependencies() {
        return dependencies;
    }

    public List<UUID> getConstraints() {
        return constraints;
    }

    @Override
    public String toString() {
        return "NodeTask{" +
                "parentId=" + parentId +
                ", id=" + id +
                ", subTasks=" + subTasks +
                ", dependencies=" + dependencies +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeTask nodeTask = (NodeTask) o;
        return id == nodeTask.id && Objects.equals(parentId, nodeTask.parentId) && Objects.equals(subTasks, nodeTask.subTasks) && Objects.equals(dependencies, nodeTask.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, id, subTasks, dependencies);
    }
}
