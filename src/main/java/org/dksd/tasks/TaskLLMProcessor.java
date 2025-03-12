package org.dksd.tasks;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.dksd.tasks.model.LinkType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

/**
 * TaskLLMProcessor processes a list of SimpleTask objects.
 * It checks if the to-do list was modified after the latest processed task,
 * sends task information to the LLM for description and scheduling, creates Task objects,
 * caches them by name, and finally sets up parent-child links.
 * It also updates the collection's latest modified timestamp.
 */
public class TaskLLMProcessor {

    private final Collection coll;
    private ChatLanguageModel model;
    private ChatLanguageModel pojoModel;

    public TaskLLMProcessor(Collection collection) {
        this.coll = collection;
        model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                //.responseFormat(JSON)
                .modelName("deepseek-r1:latest")
                .build();

        pojoModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .responseFormat(JSON)
                .modelName("mistral:latest")
                .build();

    }

    public void processSimpleTasks(List<SimpleTask> stasks) throws IOException {
        long lastTaskTime = coll.getInstance().getLatestModifiedTime();
        FileTime lastModifiedTimeOfTodoList = Files.getLastModifiedTime(coll.getInstance().getTodoFilePath());
        long listTodo = lastModifiedTimeOfTodoList.toMillis() / 1000;
        if (listTodo > lastTaskTime || lastTaskTime == 0L) {
            ConstraintExtractor constraintExtractor = AiServices.create(ConstraintExtractor.class, pojoModel);
            Map<String, Task> amp = new HashMap<>();
            for (SimpleTask stask : stasks) {
                String description = model.chat("Can you provide a description of the task name in 10-20 words: '" + stask.taskName + "' ?");
                Task task = new Task(stask.taskName, description);
                String schedule = model.chat("Can you take a guess at the scheduling of this task, when and how often we should execute or check this task: '" + task + "' also please append a cron expression of the schedule?");
                task.getMetadata().put("schedule", schedule);
                task.getMetadata().put("fileName", coll.getInstance().getTodoFilePath().toString());
                task.getMetadata().put("lineNumber", stask.line);
                coll.getInstance().addTask(task);
                System.out.println("Task: " + task.getName() + " id: " + task.getId());
                amp.put(task.getName(), task);
                try {
                    Constr constr = constraintExtractor.extractConstraintFrom(task.toString());
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
}
