package org.dksd.tasks;

import net.redhogs.cronparser.CronExpressionDescriptor;
import org.dksd.tasks.model.Concentration;
import org.dksd.tasks.model.Cost;
import org.dksd.tasks.model.DeadlineType;
import org.dksd.tasks.model.Effort;
import org.dksd.tasks.model.Importance;
import org.dksd.tasks.model.LeadTime;

import java.util.Objects;
import java.util.UUID;

public class Constraint implements Identifier {

    private UUID id;
    private String schedule; // "* * * etc
    private String scheduleDescription;
    private LeadTime leadTime; //How much time needed before deadlines in seconds etc
    private Effort effort;
    private Cost cost;
    private Importance importance;
    private Concentration concentration;
    private DeadlineType deadlineType;

    public Constraint() {
        this.id = UUID.randomUUID();
        this.schedule = "30 22 * * 1"; // Every Monday at 10:30 PM
        this.scheduleDescription = setSchedDesc(schedule);
        this.leadTime = LeadTime.ONE_DAY;
        this.effort = Effort.MEDIUM;
        this.cost = Cost.CHEAP;
        this.importance = Importance.NOT_URGENT_IMPORTANT;
        this.concentration = Concentration.PARTIAL;
        this.deadlineType = DeadlineType.SOFT;
    }

    public Constraint(Constr constr) {
        this.id = UUID.randomUUID();
        this.schedule = constr.schedule;
        this.scheduleDescription = constr.scheduleDescription;
        this.leadTime = constr.leadTime;
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
            //e.printStackTrace();
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
        setSchedDesc(schedule);
        this.schedule = schedule;
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
        return deadlineType;
    }

    public void setDeadlineType(DeadlineType deadlineType) {
        this.deadlineType = deadlineType;
    }

    public LeadTime getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(LeadTime leadTime) {
        this.leadTime = leadTime;
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
            deadlineType = DeadlineType.SOFT;
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

    @Override
    public String toString() {
        return "Constraint{" +
                "schedule='" + schedule + '\'' +
                "scheduleDescription='" + scheduleDescription + '\'' +
                ", effort=" + effort +
                ", cost=" + cost +
                ", importance=" + importance +
                ", concentration=" + concentration +
                ", deadlineType=" + deadlineType +
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
        return Objects.equals(id, that.id) && Objects.equals(schedule, that.schedule) && Objects.equals(scheduleDescription, that.scheduleDescription) && leadTime == that.leadTime && effort == that.effort && cost == that.cost && importance == that.importance && concentration == that.concentration && deadlineType == that.deadlineType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, schedule, scheduleDescription, leadTime, effort, cost, importance, concentration, deadlineType);
    }
}
