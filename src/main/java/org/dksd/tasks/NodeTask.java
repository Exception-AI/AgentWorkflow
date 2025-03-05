package org.dksd.tasks;

import java.util.ArrayList;
import java.util.List;

public class NodeTask {

    private Long parentId;
    private final long id;
    private final List<Long> subTasks = new ArrayList<>();
    private final List<Long> dependencies = new ArrayList<>();

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
}
