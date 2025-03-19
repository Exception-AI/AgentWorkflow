package org.dksd.tasks.model;

import dev.langchain4j.model.output.structured.Description;

public enum Cost {
    @Description("Cheap: Represents a minimal expense.")
    CHEAP("\uD83D\uDCB0"),

    @Description("Costly: Represents a moderate expense with two money emojis.")
    COSTLY("\uD83D\uDCB0\uD83D\uDCB0"),

    @Description("Expensive: Represents a high expense with three money emojis.")
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
