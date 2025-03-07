package org.dksd.tasks.model;

public enum LeadTime {
    ONE_DAY(1),
    TWO_DAYS(2),
    ONE_WEEK(7),
    ONE_MONTH(30),
    ONE_YEAR(365);

    private final int value;
    LeadTime(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
