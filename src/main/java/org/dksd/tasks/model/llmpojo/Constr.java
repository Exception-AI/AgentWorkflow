package org.dksd.tasks.model.llmpojo;

import org.dksd.tasks.model.Concentration;
import org.dksd.tasks.model.Cost;
import org.dksd.tasks.model.DeadlineType;
import org.dksd.tasks.model.Effort;
import org.dksd.tasks.model.Importance;
import org.dksd.tasks.model.LeadTime;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public class Constr implements Serializable {
    public String schedule; // "* * * etc
    public String scheduleDescription;
    public Set<DayOfWeek> daysOfWeek;
    public int durationSeconds;
    public LocalTime endTime;
    public int leadTimeSeconds; //How much time needed before deadlines in seconds etc
    public Effort effort;
    public Cost cost;
    public Importance importance;
    public Concentration concentration;
    public DeadlineType deadlineType;
}
