package org.dksd.tasks.cache;

import dev.langchain4j.model.output.structured.Description;
import org.dksd.tasks.model.llmpojo.Constr;

import java.io.Serializable;
import java.util.List;

public class TaskModel implements Serializable {

    @Description("Short name for the task used for identification")
    public String shortTaskName;

    @Description("Detailed description of the task")
    public String description;

    @Description("List of proposed sub-task names for this task")
    public List<String> proposedSubTaskNames;

    @Description("Constraint object that defines additional scheduling constraints for the task")
    public Constr constr;
}
