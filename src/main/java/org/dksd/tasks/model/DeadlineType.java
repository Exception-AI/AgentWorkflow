package org.dksd.tasks.model;

public enum DeadlineType {
    ASAP("\uD83C\uDD98\uD83D\uDCC5"),
    HARD("⏰\uD83D\uDCC5"),
    SOFT("\uD83E\uDD1E");

    private final String value;
    DeadlineType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
