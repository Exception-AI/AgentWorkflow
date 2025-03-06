package org.dksd.tasks;

public class Constraint {

    private String schedule; // "* * * etc
    private float leadTime; //How much time needed before deadlines in secondsetc
    private Effort effort;
    private double cost;
    private Difficulty difficulty;
    private Importance importance;
    private Concentration concentration;
    private DeadlineType deadlineType;
    private double completed; //0 - 1 range
    /*
    Character	Meaning	Example
*	All. Represents that the schedule should run for every time unit	A “*” in the minute field indicates that the schedule runs every minute
?	Any. Represents any arbitrary value. This can be used only in day-of-month and day-of-week fields	A “?” in day-of-month field will not use the day-of-month for deciding the schedule as any value is acceptable here
–	Range. Represents a continuous range of values.	Using “5-8” in the <hour> field indicates the hours 5, 6, 7 and 8
,	Multiple Values. Separates a list of different values	Using “5, 6, 10” in the <hour> field indicates the hours 5, 6 and 10
/	Increment. Specifies the amount by which to increment the values of a field	3/5 in the minute field indicates the minutes 3, 8, 13, …, 58 in an hour. star/10 in the minute field indicates the minutes 0, 10, 20…, 60
     */

    /*
    cron-utils:
It provides functionalities to define, parse, validate, and migrate cron expressions. It also offers human-readable descriptions for cron expressions and includes modules for Spring framework integration and job scheduling.
cron-expression:
This library focuses on parsing cron expressions and building corresponding Java objects. It allows checking if a cron expression matches a ZonedDateTime object and provides optional integration with java.util.concurrent.
JavaCron:
A library for parsing crontab expressions and calculating the next run time based on a current or specified date and time.
     */
    //effort
    //money
    //difficulty
    //concentration required for task
    //importance Eisenhower matrix
    //   Urgent + Important: Do it
    //   Not Urgent + Important: Schedule it
    //   Urgent + Not important: delegate it
    //   Not Urgent + Not Important: Delete it, eg Social Media

    public Constraint() {

    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Effort getEffort() {
        return effort;
    }

    public void setEffort(Effort effort) {
        this.effort = effort;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Importance getImportance() {
        return importance;
    }

    public void setImportance(Importance importance) {
        this.importance = importance;
    }

    public Concentration getConcentration() {
        return concentration;
    }

    public void setConcentration(Concentration concentration) {
        this.concentration = concentration;
    }

    public DeadlineType getDeadlineType() {
        return deadlineType;
    }

    public void setDeadlineType(DeadlineType deadlineType) {
        this.deadlineType = deadlineType;
    }

    public double getCompleted() {
        return completed;
    }

    public void setCompleted(double completed) {
        this.completed = completed;
    }

    public float getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(float leadTime) {
        this.leadTime = leadTime;
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "schedule='" + schedule + '\'' +
                ", effort=" + effort +
                ", cost=" + cost +
                ", difficulty=" + difficulty +
                ", importance=" + importance +
                ", concentration=" + concentration +
                ", deadlineType=" + deadlineType +
                ", completed=" + completed +
                '}';
    }
}
