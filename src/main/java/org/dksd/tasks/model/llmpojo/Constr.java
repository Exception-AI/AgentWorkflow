package org.dksd.tasks.model.llmpojo;

import dev.langchain4j.model.output.structured.Description;
import org.dksd.tasks.model.Concentration;
import org.dksd.tasks.model.Cost;
import org.dksd.tasks.model.DeadlineType;
import org.dksd.tasks.model.Effort;
import org.dksd.tasks.model.Importance;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public class Constr implements Serializable {
    @Description("The days the task should be completed on")
    public Set<DayOfWeek> daysOfWeek;

    @Description("Duration in seconds that the task is expected to run")
    public int durationSeconds;

    @Description("The deadline time for the task")
    public LocalTime deadlineTime;

    @Description("Allowed seconds before the deadline that indicate how early the task can be started")
    public int allowedSecondsBeforeDeadline;

    @Description("The effort level required by the task")
    public Effort effort;

    @Description("The cost level associated with the task")
    public Cost cost;

    @Description("The importance of the task")
    public Importance importance;

    @Description("The concentration level required for the task")
    public Concentration concentration;

    @Description("The type of deadline for the task")
    public DeadlineType deadlineType;
}
