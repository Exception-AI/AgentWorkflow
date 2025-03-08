package org.dksd.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dksd.tasks.model.LinkType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class Instance {

    private UUID instanceId;
    private String instanceName;
    private String instanceDescription;
    private List<Task> tasks = null;
    private Map<UUID, Task> taskMap = new HashMap<>();
    private List<Link> links = null;
    private List<Constraint> constraints = null;
    private final Map<UUID, Constraint> constraintMap = new HashMap<>();
    private final TreeMap<UUID, NodeTask> taskNodeMap = new TreeMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Task ROOT = new Task(UUID.nameUUIDFromBytes("0L".getBytes()), "ROOT", "ROOT");

    public Instance(File taskFile, File linksFile, File constraintsFile) {
        taskMap = new HashMap<>();
        tasks = loadTasks(taskFile);
        links = loadLinks(linksFile);
        constraints = loadConstraints(constraintsFile);
        taskMap.put(ROOT.getId(), ROOT);
        if (!tasks.contains(ROOT)) {
            tasks.add(ROOT);
        }
        taskNodeMap.put(ROOT.getId(), new NodeTask(ROOT.getId()));
        for (Task task : tasks) {
            taskMap.put(task.getId(), task);
            NodeTask t = new NodeTask(task.getId());
            taskNodeMap.put(task.getId(), t);
        }
        for (Link link : links) {
            addLinkToTree(link);
        }
    }

    public Instance(String tasks, String links, String constraints) {
        this(new File(tasks), new File(links), new File(constraints));
    }

    public Task createCommonTask(Task parent, String name, String desc) {
        assert parent != null;
        UUID nKey = UUID.randomUUID();//(taskMap.isEmpty()) ? 1 : Collections.max(taskMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        getTasks().add(task);
        getTaskMap().put(task.getId(), task);
        NodeTask t = new NodeTask(task.getId());
        getTaskNodeMap().put(task.getId(), t);
        t.setParentId(parent.getId());
        addLink(parent.getId(), LinkType.PARENT, task.getId());
        addConstraint(task);
        return task;
    }

    public Task createSubTask(Task parent, String name, String desc) {
        Task task = createCommonTask(parent, name, desc);
        addLink(parent.getId(), LinkType.SUBTASK, task.getId());
        return task;
    }

    public Task createDepTask(Task parent, String name, String desc) {
        Task task = createCommonTask(parent, name, desc);
        addLink(parent.getId(), LinkType.DEPENDENCY, task.getId());
        return task;
    }

    public Task createProjectTask(String name, String desc) {
        return createSubTask(ROOT, name, desc);
    }

    public void addLinkToTree(Link link) {
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

    public List<Task> loadTasks(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Task>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Link> loadLinks(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Link>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Constraint> loadConstraints(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Constraint>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getInstanceDescription() {
        return instanceDescription;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Map<UUID, Task> getTaskMap() {
        return taskMap;
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public Map<UUID, Constraint> getConstraintMap() {
        return constraintMap;
    }

    public TreeMap<UUID, NodeTask> getTaskNodeMap() {
        return taskNodeMap;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public Link addLink(UUID left, LinkType linkType, UUID right) {
        Link link = new Link(left, linkType, right);
        links.add(link);
        addLinkToTree(link);
        return link;
    }

    public Constraint addConstraint(Task task) {
        Constraint constraint = new Constraint();
        constraints.add(constraint);
        constraintMap.put(constraint.getConstraintId(), constraint);
        addLink(task.getId(), LinkType.CONSTRAINT, constraint.getConstraintId());
        return constraint;
    }
}
