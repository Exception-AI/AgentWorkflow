package org.dksd.tasks.model;

import dev.langchain4j.model.output.structured.Description;

public enum DeadlineType {
    @Description("ASAP: The deadline is as soon as possible.")
    ASAP("\uD83C\uDD98\uD83D\uDCC5"),

    @Description("HARD: A strict deadline that must be met.")
    HARD("⏰\uD83D\uDCC5"),

    @Description("ANYTIME_ON_DAY: The task can be completed anytime during the day.")
    ANYTIME_ON_DAY("\uD83E\uDD1E"),

    @Description("ANYTIME_WEEK: The task can be completed anytime during the week.")
    ANYTIME_WEEK("\uD83E\uDD1E"),

    @Description("ANYTIME_MONTH: The task can be completed anytime during the month.")
    ANYTIME_MONTH("\uD83E\uDD1E"),

    @Description("ANYTIME_BEFORE: The task can be completed anytime before a given time.")
    ANYTIME_BEFORE("\uD83E\uDD1E"),

    @Description("ANYTIME_AFTER: The task can be completed anytime after a given time.")
    ANYTIME_AFTER("\uD83E\uDD1E");

    private final String value;
    DeadlineType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public DeadlineType next(DeadlineType current) {
        if (current.ordinal() < DeadlineType.values().length - 1) {
            return DeadlineType.values()[current.ordinal() + 1];
        }
        return DeadlineType.values()[0];
    }

}
