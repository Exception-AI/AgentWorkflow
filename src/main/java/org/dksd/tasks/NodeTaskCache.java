package org.dksd.tasks;

import org.dksd.tasks.model.LinkType;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class NodeTaskCache {

    private final List<Task> tasks;
    private final List<Link> links;
    private final TreeMap<UUID, NodeTask> taskNodeMap = new TreeMap<>();

    public NodeTaskCache(List<Task> tasks, List<Link> links) {
        this.tasks = tasks;
        this.links = links;
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
    }

    public NodeTask get(UUID id) {
        for (Task task : tasks) {
            NodeTask t = new NodeTask(task.getId());
            taskNodeMap.put(id, t);
        }
        for (Link link : links) {
            addLinkToTree(link);
        }
        return taskNodeMap.get(id);
    }
}
