package org.dksd.tasks.scheduling;

import org.dksd.tasks.model.Constraint;
import org.dksd.tasks.model.NodeTask;

import java.time.DayOfWeek;

public class ScheduledTask {

    private NodeTask task;
    private final String taskName;
    private DayOfWeek endDay;
    private final Constraint constraint;

    public ScheduledTask(NodeTask task, String taskName, DayOfWeek day, Constraint constraint) {
        this.task = task;
        this.taskName = taskName;
        this.endDay = day;
        this.constraint = constraint;
    }

    public NodeTask getTask() {
        return task;
    }

    public DayOfWeek getEndDay() {
        return endDay;
    }


    public void setTask(NodeTask task) {
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
                "task=" + task.getId() +
                ", day=" + endDay +
                '}';
    }
}
