package org.dksd.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dksd.tasks.model.LinkType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Instance implements Identifier {

    private final UUID id;
    private final String instanceName;
    private String instanceDescription;
    private List<Task> tasks = null;
    private List<Link> links = null;
    private List<Constraint> constraints = null;
    private Cache<Task> taskMap= null;
    private Cache<Constraint> constraintMap = null;
    private NodeTaskCache nodeTaskCache = null;
    private final ObjectMapper mapper = new ObjectMapper();

    public Instance(String instanceName) {
        this.id = UUID.randomUUID();
        this.instanceName = instanceName;
        File instanceDir = new File("data/" + instanceName);
        if (!instanceDir.exists()) {
            instanceDir.mkdirs();
        }
        File taskFile = new File(instanceDir, "tasks.json");
        File linksFile = new File(instanceDir, "links.json");
        File constraintsFile = new File(instanceDir, "constraints.json");
        tasks = loadTasks(taskFile);
        links = loadLinks(linksFile);
        constraints = loadConstraints(constraintsFile);
        taskMap = new Cache<>(tasks);
        constraintMap = new Cache<>(constraints);
        nodeTaskCache = new NodeTaskCache(tasks, links);
    }

    public Task createCommonTask(Task parent, String name, String desc) {
        assert parent != null;
        UUID nKey = UUID.randomUUID();//(taskMap.isEmpty()) ? 1 : Collections.max(taskMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        getTasks().add(task);
        createSubTask(parent, task);
        return task;
    }

    public Task createCommonTask(Task parent, Task child) {
        assert parent != null;
        addLink(parent.getId(), LinkType.PARENT, child.getId());
        createConstraint(child);
        return child;
    }

    public Task createSubTask(Task parent, String name, String desc) {
        Task task = createCommonTask(parent, name, desc);
        addLink(parent.getId(), LinkType.SUBTASK, task.getId());
        return task;
    }

    public Task createSubTask(Task parent, Task child) {
        //TODO add exception handling.
        Task task = createCommonTask(parent, child);
        addLink(parent.getId(), LinkType.SUBTASK, task.getId());
        return task;
    }

    public Task createDepTask(Task parent, String name, String desc) {
        Task task = createCommonTask(parent, name, desc);
        addLink(parent.getId(), LinkType.DEPENDENCY, task.getId());
        return task;
    }

    public List<Task> loadTasks(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Task>>() {
            });
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Link> loadLinks(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Link>>() {
            });
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Constraint> loadConstraints(File file) {
        try {
            return mapper.readValue(file, new TypeReference<List<Constraint>>() {
            });
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void writeJson(String fileName, String json) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
            fileWriter.flush();
            System.out.println("Successfully saved JSON to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void write(Collection collection) {
        File instanceDir = new File("data/" + instanceName);
        if (!instanceDir.exists()) {
            instanceDir.mkdirs();
        }

        writeJson("data/" + instanceName + "/tasks.json", collection.toJson(getTasks()));
        writeJson("data/" + instanceName + "/links.json", collection.toJson(getLinks()));
        writeJson("data/" + instanceName + "/constraints.json", collection.toJson(getConstraints()));
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public Link addLink(UUID left, LinkType linkType, UUID right) {
        Link link = new Link(left, linkType, right);
        links.add(link);
        return link;
    }

    public Constraint createConstraint(Task task) {
        Constraint constraint = new Constraint();
        constraints.add(constraint);
        addLink(task.getId(), LinkType.CONSTRAINT, constraint.getId());
        return constraint;
    }

    public Constraint addConstraint(Task task, Constraint constraint) {
        constraints.add(constraint);
        addLink(task.getId(), LinkType.CONSTRAINT, constraint.getId());
        return constraint;
    }

    public Task getTask(UUID id) {
        return taskMap.get(id);
    }

    public Constraint getConstraint(UUID id) {
        return constraintMap.get(id);
    }

    public NodeTask getTaskNode(UUID id) {
        return nodeTaskCache.get(id);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public NodeTaskCache getTaskNodes() {
        return nodeTaskCache;
    }

    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            this.tasks.add(task);
        }
    }

    public Path getPath() {
        return new File("data/" + instanceName + "/" + instanceName + ".todo").toPath();
    }
}
