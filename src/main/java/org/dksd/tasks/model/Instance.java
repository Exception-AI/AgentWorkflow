package org.dksd.tasks.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dksd.tasks.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Instance implements Identifier {

    private static final Logger logger = LoggerFactory.getLogger(Instance.class);
    private final UUID id;
    private final String instanceName;
    private String instanceDescription;
    private List<Task> tasks = new ArrayList<>();
    private List<Link> links = new ArrayList<>();
    private List<Constraint> constraints = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public Instance(String instanceName) throws IOException {
        this.id = UUID.randomUUID();
        this.instanceName = instanceName;
        File instanceDir = new File("data/" + instanceName);
        if (!instanceDir.exists()) {
            instanceDir.mkdirs();
        }
        File taskFile = new File(instanceDir, "tasks.json");
        File linksFile = new File(instanceDir, "links.json");
        File constraintsFile = new File(instanceDir, "constraints.json");
        try {
            tasks = loadTasks(taskFile);
            links = loadLinks(linksFile);
            constraints = loadConstraints(constraintsFile);
        } catch (IOException e) {
            logger.error("Could not load tasks, links or constraints", e);
        }
        mapper.registerModule(new JavaTimeModule());
    }

    public Task createCommonTask(Task parent, String name, String desc) {
        assert parent != null;
        UUID nKey = UUID.randomUUID();//(taskMap.isEmpty()) ? 1 : Collections.max(taskMap.keySet()) + 1;
        Task task = new Task(nKey, name, desc);
        return createCommonTask(parent, task);
    }

    public Task createCommonTask(Task parent, Task child) {
        assert parent != null;
        getTasks().add(child);
        addLink(parent.getId(), LinkType.PARENT, child.getId());
        createConstraint(child);
        return child;
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

    public List<Task> loadTasks(File file) throws IOException {
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(file, new TypeReference<List<Task>>() {
            });
    }

    public List<Link> loadLinks(File file) throws IOException {
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(file, new TypeReference<List<Link>>() {
            });
    }

    public List<Constraint> loadConstraints(File file) throws IOException {
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(file, new TypeReference<List<Constraint>>() {
            });
    }

    public void writeJson(String fileName, String json) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
            fileWriter.flush();
        } catch (IOException e) {
            logger.error("Error writing JSON to file: ", e);
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
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    public List<Link> getLinks() {
        if (links == null) {
            links = new ArrayList<>();
        }
        return links;
    }

    public List<Constraint> getConstraints() {
        if (constraints == null) {
            constraints = new ArrayList<>();
        }
        return constraints;
    }

    public List<Constraint> getConstraints(Task task) {
        return links.stream()
                .filter(link -> link.getLeft() != null
                        && link.getLeft().equals(task.getId())
                        && link.getLinkType().equals(LinkType.CONSTRAINT))
                .map(link -> getConstraint(link.getRight()))
                .filter(Objects::nonNull)
                .findFirst()
                .map(List::of)
                .orElse(Collections.emptyList());
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
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Constraint getConstraint(UUID id) {
        return constraints.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            this.tasks.add(task);
        }
    }

    public Path getTodoFilePath() {
        return new File("data/" + instanceName + "/" + instanceName + ".todo").toPath();
    }

    public long getLatestModifiedTime() {
        long lastTaskTime = 0;
        for (Task task : getTasks()) {
            if (task.getLastModifiedTime() > lastTaskTime) {
                lastTaskTime = task.getLastModifiedTime();
            }
        }
        return lastTaskTime;
    }

    public Task getTaskByName(String taskName) {
        for (Task task : tasks) {
            if (task.getName().equals(taskName)) {
                return task;
            }
        }
        return null;
    }

    public void removeTask(Task currentTask) {
        this.tasks.remove(currentTask);
        //omg this could be complex.
        //need to basicaly do a VM GC algo
    }

    public boolean isParent(UUID id) {
        for (Link link : this.links) {
            if (link.getLeft() != null && link.getLeft().equals(id) && link.getLinkType().equals(LinkType.PARENT)) {
                return true;
            }
        }
        return false;
    }

    /*public String getHierarchy(Task nodeTask) {
        NodeTask ht = nodeTask;
        List<String> hierarchy = new ArrayList<>();
        while (ht.getParentId() != null) {
            ht = getTaskNode(ht.getParentId());
            hierarchy.add(getTask(ht.getId()).getName());
        }
        return hierarchy.toString();
    }*/

    public boolean isLeaf(NodeTask nodeTask) {
        return nodeTask.getSubTasks().isEmpty();
    }

    /*private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private WatchService watchService;

    public void pollEvents() {
        try {
            WatchKey key = watchService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                // Handle the specific event
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    System.out.println("File created: " + event.context());
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    System.out.println("File deleted: " + event.context());
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    System.out.println("File modified: " + event.context());
                }
            }

            // To receive further events, reset the key
            key.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void watchDir() {
        try {
            // Specify the directory which supposed to be watched
            Path directoryPath = Paths.get("data/" + instanceName);

            // Create a WatchService
            watchService = FileSystems.getDefault().newWatchService();

            // Register the directory for specific events
            directoryPath.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watching directory: " + directoryPath);

        } catch (Exception ep) {
            ep.printStackTrace();
        }

    }*/
}
