package org.dksd.tasks.scheduling;

import org.dksd.tasks.model.Constraint;
import org.dksd.tasks.model.Task;

import java.time.DayOfWeek;

public class ScheduledTask {

    private Task task;
    private final String taskName;
    private DayOfWeek endDay;
    private final Constraint constraint;

    public ScheduledTask(Task task, String taskName, DayOfWeek day, Constraint constraint) {
        this.task = task;
        this.taskName = taskName;
        this.endDay = day;
        this.constraint = constraint;
    }

    public Task getTask() {
        return task;
    }

    public DayOfWeek getEndDay() {
        return endDay;
    }


    public void setTask(Task task) {
        this.task = task;
    }

    public void setEndDay(DayOfWeek endDay) {
        this.endDay = endDay;
    }


    public Constraint getConstraint() {
        return constraint;
    }

    public String getTaskName() {
        return taskName;
    }

    @Override
    public String toString() {
        return "ScheduledTask{" +
                "  taskName='" + taskName + '\'' +
                ", endDay=" + endDay +
                ", constraint=" + constraint +
                '}';
    }
}
