package org.dksd.tasks;

import org.dksd.tasks.cache.ModelCache;
import org.dksd.tasks.model.LinkType;

import java.io.IOException;
import java.util.List;

/**
 * TaskLLMProcessor processes a list of SimpleTask objects.
 * It checks if the to-do list was modified after the latest processed task,
 * sends task information to the LLM for description and scheduling, creates Task objects,
 * caches them by name, and finally sets up parent-child links.
 * It also updates the collection's latest modified timestamp.
 */
public class TaskLLMProcessor {

    private final Collection coll;
    private ModelCache modelCache;

    public TaskLLMProcessor(Collection collection) {
        this.coll = collection;
        this.modelCache = new ModelCache();
    }

    public void createSimpleTask(String parent, String child) {
        boolean taskExists = coll.getInstance().containsTaskName(child);
        if (parent == null && !taskExists) {
            createTask(null, child);
        } else {
            boolean parentExists = coll.getInstance().containsTaskName(parent);
            if (parentExists && !taskExists) {
                createTask(null, child);
            }
        }
    }

    public void processSimpleTasks(List<SimpleTask> stasks) throws IOException {
        for (int i = 0; i < 5; i++) {
            for (SimpleTask stask : stasks) {
                createSimpleTask(stask.parentTask, stask.taskName);
            }
        }
        System.out.println("Processed simple tasks: " + stasks.size());
    }

    public Task createTask(Task parent, String childName) {
        String description = modelCache.chat("Can you provide a description of the task name in 10-20 words: '" + childName + "' ?");
        Task task = new Task(childName, description);
        String schedule = modelCache.chat("Can you take a guess at the scheduling of this task, when and how often we should execute or check this task: '" + task.getName() + "' also please append a cron expression of the schedule?");
        task.getMetadata().put("schedule", schedule);
        task.getMetadata().put("fileName", coll.getInstance().getTodoFilePath().toString());
        //task.getMetadata().put("lineNumber", stask.line);
        coll.getInstance().addTask(task);
        System.out.println("Task: " + task.getName() + " id: " + task.getId());
        try {
            Constr constr = modelCache.extractConstraintFrom(task.getName() + " : " + task.getDescription());
            coll.getInstance().addConstraint(task, new Constraint(constr));
            coll.getInstance().write(coll);
        } catch (Exception ep) {
            ep.printStackTrace();
        }
        if (parent == null) {
            coll.getInstance().addLink(null, LinkType.PARENT, task.getId());
        }
        if (parent != null) {
            coll.getInstance().addLink(parent.getId(), LinkType.PARENT, task.getId());
            coll.getInstance().addLink(parent.getId(), LinkType.SUBTASK, task.getId());
        }
        return task;
    }

    public void createSubTaskFromParent(String parentName, String parentDescription) {
        //Need to pass in parent and all subtasks so far so it can suggest a new task.
        //Task l;imited to depth 1
        //String subTaskName = modelCache.chat("Can you create a new sub task to help divide and conquer this task and description: 'name=" + name + ", description='"+description+" ?");
        //createSimpleTask(parentName, subTaskName);
    }
}
