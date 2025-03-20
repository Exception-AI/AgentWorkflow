package org.dksd.tasks.cache;

import org.dksd.tasks.model.Link;
import org.dksd.tasks.model.NodeTask;
import org.dksd.tasks.model.Task;
import org.dksd.tasks.model.LinkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NodeTaskCache {

    private static final Logger logger = LoggerFactory.getLogger(NodeTaskCache.class);
    private final List<Task> tasks;
    private final List<Link> links;
    private final Map<UUID, NodeTask> taskNodeMap = new HashMap<>();

    public NodeTaskCache(List<Task> tasks, List<Link> links) {
        this.tasks = Collections.synchronizedList(new ArrayList<>(tasks));
        this.links = Collections.synchronizedList(new ArrayList<>(links));;
    }

    private void addLinkToTree(Link link) {
        UUID left = link.getLeft();
        UUID right = link.getRight();
        logger.debug("Left " + left + " right: " + right + " ln " + taskNodeMap.get(left) + " rn " + taskNodeMap.get(right));
        if (LinkType.PARENT.equals(link.getLinkType())) {
            taskNodeMap.get(right).setParentId(left);
        }
        if (LinkType.CHILD.equals(link.getLinkType())) {
            taskNodeMap.get(left).setParentId(right);
        }
        if (LinkType.SUBTASK.equals(link.getLinkType())) {
            taskNodeMap.get(left).getSubTasks().add(right);
        }
        if (LinkType.DEPENDENCY.equals(link.getLinkType())) {
            taskNodeMap.get(left).getDependencies().add(right);
        }
        if (LinkType.CONSTRAINT.equals(link.getLinkType())) {
            taskNodeMap.get(left).getConstraints().add(right);
        }
    }

    public NodeTask get(UUID id) {
        try {
            for (Task task : tasks) {
                NodeTask t = new NodeTask(task.getId());
                taskNodeMap.put(task.getId(), t);
            }
            for (Link link : links) {
                addLinkToTree(link);
            }
            return taskNodeMap.get(id);
        } catch (Exception e) {
            logger.error("Could not get id: " + id);
            //System.exit(1);
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
