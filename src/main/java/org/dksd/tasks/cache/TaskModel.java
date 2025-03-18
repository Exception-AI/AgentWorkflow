package org.dksd.tasks.cache;

import org.dksd.tasks.model.llmpojo.Constr;

import java.io.Serializable;
import java.util.List;

public class TaskModel implements Serializable {

    public String shortTaskName;
    public String description;
    public String cronSchedule;
    public List<String> proposedSubTaskNames;
    public Constr constr;
}
