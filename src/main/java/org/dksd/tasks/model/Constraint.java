package org.dksd.tasks.model;

import net.redhogs.cronparser.CronExpressionDescriptor;
import org.dksd.tasks.Identifier;
import org.dksd.tasks.model.llmpojo.Constr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

public class Constraint implements Identifier {

    private static final Logger logger = LoggerFactory.getLogger(Constraint.class);
    private UUID id;
    private String schedule; // "* * * etc
    private String scheduleDescription;
    private Set<DayOfWeek> daysOfWeek;
    private int durationSeconds;
    private LocalTime deadlineTime;
    private int allowedSecondsBeforeDeadline; //How much time needed before deadlines in seconds the task can be started
    private Effort effort;
    private Cost cost;
    private Importance importance;
    private Concentration concentration;
    private DeadlineType deadlineType;

    public Constraint() {
        this.id = UUID.randomUUID();
        setBase();
    }

    private void setBase() {
        this.schedule = "30 22 * * 1"; // Every Monday at 10:30 PM
        this.scheduleDescription = setSchedDesc(schedule);
        this.durationSeconds = 30*60;
        this.daysOfWeek = new HashSet<>();
        daysOfWeek.add(MONDAY);
        daysOfWeek.add(TUESDAY);
        daysOfWeek.add(WEDNESDAY);
        daysOfWeek.add(THURSDAY);
        daysOfWeek.add(FRIDAY);
        daysOfWeek.add(SATURDAY);
        daysOfWeek.add(SUNDAY);
        this.deadlineTime = LocalTime.of(10, 0);
        this.allowedSecondsBeforeDeadline = 10*60;
        this.effort = Effort.MEDIUM;
        this.cost = Cost.CHEAP;
        this.importance = Importance.NOT_URGENT_IMPORTANT;
        this.concentration = Concentration.PARTIAL;
        this.deadlineType = DeadlineType.ANYTIME_ON_DAY;
    }

    public Constraint(Constr constr) {
        this.id = UUID.randomUUID();
        if (constr == null) {
            setBase();
            return;
        }
        this.schedule = constr.schedule;
        this.scheduleDescription = constr.scheduleDescription;
        this.durationSeconds = constr.durationSeconds;
        this.daysOfWeek = constr.daysOfWeek;
        this.deadlineTime = constr.deadlineTime;
        this.allowedSecondsBeforeDeadline = constr.allowedSecondsBeforeDeadline;
        this.effort = constr.effort;
        this.cost = constr.cost;
        this.importance = constr.importance;
        this.concentration = constr.concentration;
        this.deadlineType = constr.deadlineType;
    }

    private String setSchedDesc(String schedule) {
        try {
            return CronExpressionDescriptor.getDescription(schedule);
        } catch (Exception e) {
            logger.error("Could not get description from cron expression");
            this.schedule = "30 22 * * 1";
        }
        return setSchedDesc(this.schedule);
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
        setSchedDesc(schedule);
    }

    public Effort getEffort() {
        return effort;
    }

    public void setEffort(Effort effort) {
        this.effort = effort;
    }

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
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
        if (deadlineType == null) {
            deadlineType = DeadlineType.ANYTIME_ON_DAY;
        }
        return deadlineType;
    }

    public void setDeadlineType(DeadlineType deadlineType) {
        this.deadlineType = deadlineType;
    }

    public int getAllowedSecondsBeforeDeadline() {
        return allowedSecondsBeforeDeadline;
    }

    public void setAllowedSecondsBeforeDeadline(int allowedSecondsBeforeDeadline) {
        this.allowedSecondsBeforeDeadline = allowedSecondsBeforeDeadline;
    }

    public String toCompactString() {
        if (cost == null) {
            cost = Cost.CHEAP;
        }
        if (importance == null) {
            importance = Importance.NOT_URGENT_IMPORTANT;
        }
        if (concentration == null) {
            concentration = Concentration.PARTIAL;
        }
        if (deadlineType == null) {
            deadlineType = DeadlineType.ANYTIME_ON_DAY;
        }
        if (effort == null) {
            effort = Effort.MEDIUM;
        }
        return importance.getValue() + ":" + effort.getValue() + ":" + cost.getValue() + ":" +
                concentration.getValue() + ":" + deadlineType.getValue();
    }

    public String getScheduleDescription() {
        return scheduleDescription;
    }

    public void setScheduleDescription(String scheduleDescription) {
        this.scheduleDescription = scheduleDescription;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        if (daysOfWeek == null) {
            daysOfWeek = new HashSet<>();
            daysOfWeek.add(MONDAY);
            daysOfWeek.add(TUESDAY);
            daysOfWeek.add(WEDNESDAY);
            daysOfWeek.add(THURSDAY);
            daysOfWeek.add(FRIDAY);
            daysOfWeek.add(SATURDAY);
            daysOfWeek.add(SUNDAY);
        }
        return daysOfWeek;
    }

    public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public LocalTime getDeadlineTime() {
        if (deadlineTime == null) {
            this.deadlineTime = LocalTime.of(10, 0);
        }
        return deadlineTime;
    }

    public void setDeadlineTime(LocalTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    @Override
    public String toString() {
        return  "daysOfWeek=" + daysOfWeek +
                ", durationMins=" + durationSeconds / 60.0 +
                ", deadline=" + deadlineTime +
                ", leadTimeMinutes=" + allowedSecondsBeforeDeadline / 60.0 +
                ", effort=" + effort +
                ", cost=" + cost +
                ", importance=" + importance +
                ", concentration=" + concentration +
                ", deadlineType=" + deadlineType +
                ", id=" + id +
                '}';
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Constraint that = (Constraint) o;
        return Objects.equals(id, that.id) && Objects.equals(schedule, that.schedule) && Objects.equals(scheduleDescription, that.scheduleDescription) && allowedSecondsBeforeDeadline == that.allowedSecondsBeforeDeadline && effort == that.effort && cost == that.cost && importance == that.importance && concentration == that.concentration && deadlineType == that.deadlineType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, schedule, scheduleDescription, allowedSecondsBeforeDeadline, effort, cost, importance, concentration, deadlineType);
    }
}
