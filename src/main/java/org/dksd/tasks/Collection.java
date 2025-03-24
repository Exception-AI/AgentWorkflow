package org.dksd.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dksd.tasks.model.Constraint;
import org.dksd.tasks.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Collection {

    private static final Logger logger = LoggerFactory.getLogger(Collection.class);
    private final List<Instance> instances = new ArrayList<>(); // save these
    private final ObjectMapper mapper = new ObjectMapper();
    private Task curr;

    public Collection(Instance... instances) {
        this.instances.addAll(Arrays.stream(instances).toList());
        mapper.registerModule(new JavaTimeModule());
        final Collection coll = this;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Instance instance : instances) {
                instance.write(coll);
                logger.info("Wrote coll");
            }
        }));
    }

    public Instance getInstance() {
        if (instances.size() == 1) {
            return instances.getFirst();
        }
        //TODO a bug below where all the tasks are not yet loaded.
        for (Instance instance : instances) {
            if (instance.getTasks().contains(getCurrentTask())) {
                return instance;
            }
        }
        return null;
    }

    public static <T> T getNextIndex(List<T> tasks, int currentIndex) {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }
        return tasks.get((currentIndex + 1) % tasks.size());
    }

    private void deleteTask(Task currentTask) {
        getInstance().removeTask(currentTask);
    }

    public void displayTasks(List<Task> path) {
        //String greenCheck = "\u001B[32m\u2713\u001B[0m";

        //Do I know the current task?
        Task task = getCurrentTask();
        if (task == null) {
            System.out.println("Hold tight while we create the tasks");
            return;
        }
        int currIndex = path.indexOf(task);
        //for (int i = getIndex(path, currIndex - 5); i < getIndex(path, currIndex + 5); i++) {
            //Task task = path.get(i);
            String suffix = (getInstance().isParent(task.getId())) ? "(P)" : "(*)";
            //if (!getInstance().isParent(task.getId())) {
                System.out.println(getInstance().getTask(task.getId()).getName() /*+ " <- " + getInstance().getHierarchy(nodeTask)*/ + " " + suffix);
                if (task.equals(getCurrentTask())) {
                    for (Constraint constraint : getInstance().getConstraints(task)) {
                        System.out.println(constraint);
                    }
                }
            //}
        //}
        /*List<String> hierarchy = new ArrayList<>();
        while (wtn.getParentId() != null) {
            wtn = getInstance().getTaskNode(wtn.getParentId());
            hierarchy.add(getInstance().getTask(wtn.getId()).getName());
        }

        wtn = getInstance().getTaskNode(wt.getId());
        String indent = "  ";
        System.out.println(wt.getName() + " <- " + hierarchy);
        //System.out.println(suffix + "   Description: " + wt.getDescription());
        System.out.flush();

        for (UUID subTask : wtn.getSubTasks()) {
            for (UUID constraint : getInstance().getTaskNode(subTask).getConstraints()) {
                System.out.print(getInstance().getConstraint(constraint).toCompactString());
            }
            System.out.println(indent + "- " + getInstance().getTask(subTask).getName());
        }
        System.out.flush();
        if (!wtn.getDependencies().isEmpty()) {
            System.out.print(indent + "  Dependencies: ");
        }
        for (UUID dep : wtn.getDependencies()) {
            System.out.print(getInstance().getTask(dep).getName() + ", ");
        }
        */
        System.out.flush();
    }

    private int getIndex(List<Task> path, int index) {
        if (index >= path.size()) {
            return path.size() - 1;
        }
        if (index < 0) {
            return 0;
        }
        return index;
    }

    public String toJson(List<?> tasks) {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void find(String searchTerm) {
        for (Task task : getInstance().getTasks()) {
            if (task.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                curr = task;
            }
        }
    }

    /*public void dfs(List<Task> path, Task nt) {
        if (nt == null)
            return;

        path.add(nt);

        for (UUID subTask : nt.getSubTasks()) {
            dfs(path, getInstance().getTaskNode(subTask));
        }
        for (UUID depTask : nt.getDependencies()) {
            dfs(path, getInstance().getTaskNode(depTask));
        }
    }

    public List<Task> getInlineTasks() {
        List<Task> path = new ArrayList<>();
        for (Task nodeTask : getRootTasks()) {
            dfs(path, nodeTask);
        }
        return path;
    }

    public Task setCurrentTask(List<NodeTask> path, Function<Integer, Integer> indexSelector) {
        System.out.println("Current node task: " + curr);

        // Find the index of the current task in the path.
        int currentIndex = IntStream.range(0, path.size())
                .filter(i -> path.get(i).getId().equals(curr.getId()))
                .findFirst()
                .orElse(-1);

        if (currentIndex == -1) {
            // If current task is not found, return the current task.
            return curr;
        }

        // Calculate new index based on provided function.
        int newIndex = indexSelector.apply(currentIndex);
        while (getInstance().isParent(path.get(newIndex).getId())) {
            newIndex = indexSelector.apply(newIndex);
        }
        curr = path.get(newIndex);

        System.out.println("Updated node task: " + curr);
        return curr;
    }

    private List<Task> getRootTasks() {
        List<NodeTask> rootNodes = new ArrayList<>();
        for (Link link : new ArrayList<>(getInstance().getLinks())) {
                if (link.getLinkType().equals(LinkType.PARENT) && link.getLeft() == null) {
                    rootNodes.add(getInstance().getTaskNode(link.getRight()));
                }
        }
        return rootNodes;
    }

    public NodeTask getCurrentNodeTask() {
        if (curr != null) {
            return curr;
        }
        if (!this.instances.getFirst().getTasks().isEmpty()) {
            curr = this.instances.getFirst().getTaskNodes().getFirst();
        }
        return null;
    }*/

    public int getTotalTaskCount() {
        int tot = 0;
        for (Instance instance : instances) {
            tot += instance.getTasks().size();
        }
        return tot;
    }

    public Task setNextTask() {
        List<Task> tasks = getInstance().getTasks();
        int indx = tasks.indexOf(getCurrentTask());
        int next = (indx + 1) % tasks.size();
        curr = tasks.get(next);
        logger.info("Setting next task to :" + curr);
        return curr;
    }

    public Task setPrevTask() {
        List<Task> tasks = getInstance().getTasks();
        int indx = tasks.indexOf(curr);
        int prev = (indx - 1) % tasks.size();
        curr = tasks.get(prev);
        logger.info("Setting prev task to :" + curr);
        return curr;
    }

    public Task getCurrentTask() {
        if (curr == null && !getInstance().getTasks().isEmpty()) {
            curr = getInstance().getTasks().getFirst();
        }
        return curr;
    }
}
