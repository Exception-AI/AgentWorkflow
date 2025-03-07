package org.dksd.tasks.model;

public enum Effort {
    HIGH("\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA"),
    MEDIUM("\uD83D\uDCAA\uD83D\uDCAA"),
    LOW("\uD83D\uDCAA");

    private final String value;
    Effort(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
