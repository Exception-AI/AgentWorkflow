package org.dksd.tasks.model;

import dev.langchain4j.model.output.structured.Description;

public enum Importance {
    @Description("Urgent & Important: This task is both high priority and critical.")
    URGENT_IMPORTANT("\uD83D\uDCE2❗\uD83D\uDEA8"),

    @Description("Urgent but Not Important: This task needs attention soon, but is not critical.")
    URGENT_NOT_IMPORTANT("\uD83E\uDD1D\uD83C\uDFFB"),

    @Description("Not Urgent but Important: This task is important but not time sensitive.")
    NOT_URGENT_IMPORTANT("\uD83D\uDCC6"),

    @Description("Not Urgent & Not Important: This task is neither critical nor time sensitive.")
    NOT_URGENT_NOT_IMPORTANT("\uD83D\uDDD1\uFE0F");

    private final String value;
    Importance(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public Importance next(Importance current) {
        if (current.ordinal() < Importance.values().length - 1) {
            return Importance.values()[current.ordinal() + 1];
        }
        return Importance.values()[0];
    }
}
