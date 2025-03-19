package org.dksd.tasks.model;

import dev.langchain4j.model.output.structured.Description;

public enum Concentration {
    @Description("Full concentration: The task requires complete focus. Emoji: \uD83E\uDDE0")
    FULL("\uD83E\uDDE0"),

    @Description("Partial concentration: The task requires moderate focus. Emoji: \uD83E\uDD14")
    PARTIAL("\uD83E\uDD14"),

    @Description("Minimal concentration: The task requires minimal focus. Emoji: \uD83D\uDE35\u200D\uD83D\uDCAB")
    MINIMAL("\uD83D\uDE35\u200D\uD83D\uDCAB");

    private final String value;
    Concentration(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public Concentration next(Concentration current) {
        if (current.ordinal() < Concentration.values().length - 1) {
            return Concentration.values()[current.ordinal() + 1];
        }
        return Concentration.values()[0];
    }
}
