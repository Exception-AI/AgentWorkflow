package org.dksd.tasks;

import dev.langchain4j.service.UserMessage;

public interface TaskExtractor {
    @UserMessage("Return the task from {{it}} there may be a name and a schedule component, also explain why")
    SimpleTask extractTaskFrom(String text);
}
