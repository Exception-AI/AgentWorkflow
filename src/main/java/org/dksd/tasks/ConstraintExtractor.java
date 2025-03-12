package org.dksd.tasks;

import dev.langchain4j.service.UserMessage;

public interface ConstraintExtractor {
    @UserMessage("Return the constraint from {{it}} also explain why")
    Constr extractConstraintFrom(String text);
}
