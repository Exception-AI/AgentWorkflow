package org.dksd.tasks;

import org.dksd.tasks.cache.ModelCache;
import org.dksd.tasks.cache.TaskModel;
import org.dksd.tasks.model.Constraint;
import org.dksd.tasks.model.LinkType;
import org.dksd.tasks.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TaskLLMProcessor processes a list of SimpleTask objects.
 * It checks if the to-do list was modified after the latest processed task,
 * sends task information to the LLM for description and scheduling, creates Task objects,
 * caches them by name, and finally sets up parent-child links.
 * It also updates the collection's latest modified timestamp.
 */
public class TaskLLMProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TaskLLMProcessor.class);
    private final Collection coll;
    private final ModelCache modelCache;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    public TaskLLMProcessor(Collection collection) {
        this.coll = collection;
        this.modelCache = new ModelCache();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownPool));
    }

    public void shutdownPool() {
        threadPool.shutdown();
        try {
            // Wait for all tasks to finish, with a timeout of 60 seconds
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                // Force shutdown if tasks did not finish in time
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean createSimpleTask(String parent, String child) {
        Instance in = coll.getInstance();
        if (in.getTaskByName(child) == null) {
            String p = "";
            if (in.getTaskByName(parent) != null) {
                p = in.getTaskByName(parent).getName();
            }
            createTask(in.getTaskByName(parent), p, child);
            return true;
        }
        return false;
    }

    public void processSimpleTasks(List<SimpleTask> stasks) {
        stasks.forEach(stask ->
                threadPool.submit(() -> {
                    if (createSimpleTask(stask.parentTask, stask.taskName)) {
                        logger.info("Created task: " + stask.taskName);
                    }
                })
        );
    }

    public Task createTask(Task parent, String parentName, String taskName) {
        //task.getMetadata().put("lineNumber", stask.line);
        try {
            TaskModel taskModel = modelCache.extractTaskModelFrom(parentName, taskName);
            Task task = new Task();
            task.setName(taskName);
            task.setDescription(taskModel.description);
            coll.getInstance().addTask(task);
            task.getMetadata().put("fileName", coll.getInstance().getTodoFilePath().toString());
            task.getMetadata().put("taskId", task.getId());
            task.getMetadata().put("proposedSubTasks", taskModel.proposedSubTaskNames);
            task.getMetadata().put("proposedName", taskModel.shortTaskName);
            if (parent == null) {
                coll.getInstance().addLink(null, LinkType.PARENT, task.getId());
            }
            if (parent != null) {
                coll.getInstance().addLink(parent.getId(), LinkType.PARENT, task.getId());
                coll.getInstance().addLink(parent.getId(), LinkType.SUBTASK, task.getId());
            }
            coll.getInstance().addConstraint(task, new Constraint(taskModel.constr));
            coll.getInstance().write(coll);
            return task;
        } catch (Exception ep) {
            ep.printStackTrace();
        }
        return null;
    }

    /*public void createAutoSubTaskFromParent(Task parent) {
        String taskView = coll.getInstance().getTaskView(parent);
        String subTaskName = modelCache.chat("Please provide only the final name as name='<name goes here>'. Divide and conquer this task into one additional sub task: '" + taskView);
        createTask(parent, subTaskName.substring(subTaskName.indexOf("</think>") + "</think>".length()).trim());
    }*/
}
