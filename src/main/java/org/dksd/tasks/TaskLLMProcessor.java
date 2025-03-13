package org.dksd.tasks;

import org.dksd.tasks.model.LinkType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void processSimpleTasks(List<SimpleTask> stasks) throws IOException {
        Map<String, Task> amp = new HashMap<>();
        for (SimpleTask stask : stasks) {
            //Want to combine these into one call instead of two
            String description = modelCache.chat("Can you provide a description of the task name in 10-20 words: '" + stask.taskName + "' ?");
            Task task = new Task(stask.taskName, description);
            String schedule = modelCache.chat("Can you take a guess at the scheduling of this task, when and how often we should execute or check this task: '" + task + "' also please append a cron expression of the schedule?");
            task.getMetadata().put("schedule", schedule);
            task.getMetadata().put("fileName", coll.getInstance().getTodoFilePath().toString());
            task.getMetadata().put("lineNumber", stask.line);
            coll.getInstance().addTask(task);
            System.out.println("Task: " + task.getName() + " id: " + task.getId());
            amp.put(task.getName(), task);
            try {
                Constr constr = modelCache.extractConstraintFrom(task.toString());
                coll.getInstance().addConstraint(task, new Constraint(constr));
                coll.getInstance().write(coll);
            } catch (Exception ep) {
                ep.printStackTrace();
            }
        }
        for (SimpleTask stask : stasks) {
            Task parent = amp.get(stask.parentTask);
            Task child = amp.get(stask.taskName);
            if (parent == null && child != null) {
                coll.getInstance().addLink(null, LinkType.PARENT, child.getId());
            }
            if (parent != null && child != null) {
                coll.getInstance().addLink(parent.getId(), LinkType.PARENT, child.getId());
                coll.getInstance().addLink(parent.getId(), LinkType.SUBTASK, child.getId());
            }
        }
        coll.getInstance().write(coll);
    }
}
