package org.dksd.tasks.model;

public enum Importance {
    URGENT_IMPORTANT("\uD83D\uDCE2❗\uD83D\uDEA8"),
    URGENT_NOT_IMPORTANT("\uD83E\uDD1D\uD83C\uDFFB"),
    NOT_URGENT_IMPORTANT("\uD83D\uDCC6"),
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
