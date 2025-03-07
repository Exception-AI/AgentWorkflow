package org.dksd.tasks.model;

public enum Concentration {
    FULL("\uD83E\uDDE0"),
    PARTIAL("\uD83E\uDD14"),
    MINIMAL("\uD83D\uDE35\u200D\uD83D\uDCAB");

    private final String value;
    Concentration(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
