package org.dksd.tasks.model;

import dev.langchain4j.model.output.structured.Description;

public class SimpleTask {
    @Description("The name of the task which is also used to identify the task")
    public String taskName;
    @Description("The description of the task which can be added to or expanded upon")
    String description;
    @Description("Schedule of the task or how often it happens or when it should happen")
    String schedule;
    @Description("The name of the parent task which is usually one level of indentation less")
    public String parentTask;
    public int indent;
    public int line;
}
