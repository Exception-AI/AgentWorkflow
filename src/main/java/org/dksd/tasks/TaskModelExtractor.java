package org.dksd.tasks;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.dksd.tasks.cache.TaskModel;

public interface TaskModelExtractor {
    @SystemMessage(fromResource = "SystemPromptTask.txt")
    @UserMessage(fromResource = "UserPromptTask.txt")
    TaskModel extractTaskModelFrom(@V("parent") String parent, @V("child") String child);
}
