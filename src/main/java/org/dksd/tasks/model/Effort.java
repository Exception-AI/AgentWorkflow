package org.dksd.tasks.model;

import dev.langchain4j.model.output.structured.Description;

public enum Effort {
    @Description("High effort: Requires significant energy and focus.")
    HIGH("\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA"),

    @Description("Medium effort: Requires moderate energy and focus.")
    MEDIUM("\uD83D\uDCAA\uD83D\uDCAA"),

    @Description("Low effort: Requires minimal energy and focus.")
    LOW("\uD83D\uDCAA");

    private final String value;
    Effort(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public Effort next(Effort current) {
        if (current.ordinal() < Effort.values().length - 1) {
            return Effort.values()[current.ordinal() + 1];
        }
        return Effort.values()[0];
    }
}
