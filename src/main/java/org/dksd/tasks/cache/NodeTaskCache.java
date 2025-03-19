package org.dksd.tasks.cache;

import org.dksd.tasks.model.Link;
import org.dksd.tasks.model.NodeTask;
import org.dksd.tasks.model.Task;
import org.dksd.tasks.model.LinkType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NodeTaskCache {

    private final List<Task> tasks;
    private final List<Link> links;
    private final Map<UUID, NodeTask> taskNodeMap = new HashMap<>();

    public NodeTaskCache(List<Task> tasks, List<Link> links) {
        this.tasks = Collections. synchronizedList(new ArrayList<>(tasks));
        this.links = Collections. synchronizedList(new ArrayList<>(links));;
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
        if (LinkType.CONSTRAINT.equals(link.getLinkType())) {
            taskNodeMap.get(link.getLeft()).getConstraints().add(link.getRight());
        }
    }

    public NodeTask get(UUID id) {
        for (Task task : tasks) {
            NodeTask t = new NodeTask(task.getId());
            taskNodeMap.put(task.getId(), t);
        }
        for (Link link : links) {
            addLinkToTree(link);
        }
        return taskNodeMap.get(id);
    }

    public Map<UUID, NodeTask> getTaskNodeMap() {
        return taskNodeMap;
    }

    public NodeTask getFirst() {
        if (tasks.isEmpty()) {
            return null;
        }
        return get(tasks.getFirst().getId());
    }

}
