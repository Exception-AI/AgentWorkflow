package org.dksd.tasks;

import java.io.Serializable;
import java.util.List;

public class TaskModel implements Serializable {

    public String shortTaskName;
    public String description;
    public String cronSchedule;
    public List<String> proposedSubTaskNames;
    public Constr constr;
}
