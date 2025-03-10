package org.dksd.tasks;

import dev.langchain4j.service.UserMessage;

public interface TaskExtractor {
    @UserMessage("Return the extracted as a task from {{it}}")
    SimpleTask extractTaskFrom(String text);
}
