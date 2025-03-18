package org.dksd.tasks;

import dev.langchain4j.service.UserMessage;
import org.dksd.tasks.cache.TaskModel;

public interface TaskModelExtractor {
    @UserMessage("Return the constraint from {{it}} also explain why")
    TaskModel extractTaskModelFrom(String text);
}
