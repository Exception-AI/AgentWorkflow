package org.dksd.tasks.model;

public enum Cost {
    CHEAP("\uD83D\uDCB0"),
    COSTLY("\uD83D\uDCB0\uD83D\uDCB0"),
    EXPENSIVE("\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0");

    private final String value;
    Cost(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Cost next(Cost current) {
        if (current.ordinal() < Cost.values().length - 1) {
            return Cost.values()[current.ordinal() + 1];
        }
        return Cost.values()[0];
    }

}
