package org.dksd.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeTask {

    private Long parentId;
    private final long id;
    private final List<Long> subTasks = new ArrayList<>();
    private final List<Long> dependencies = new ArrayList<>();
    private final List<Long> constraints = new ArrayList<>();

    public NodeTask(long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public long getId() {
        return id;
    }

    public List<Long> getSubTasks() {
        return subTasks;
    }

    public List<Long> getDependencies() {
        return dependencies;
    }

    public List<Long> getConstraints() {
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
