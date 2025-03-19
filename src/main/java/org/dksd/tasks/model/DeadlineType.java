package org.dksd.tasks.model;

public enum DeadlineType {
    ASAP("\uD83C\uDD98\uD83D\uDCC5"),
    HARD("⏰\uD83D\uDCC5"),
    ANYTIME_ON_DAY("\uD83E\uDD1E"),
    ANYTIME_WEEK("\uD83E\uDD1E"),
    ANYTIME_MONTH("\uD83E\uDD1E"),
    ANYTIME_BEFORE("\uD83E\uDD1E"),
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
