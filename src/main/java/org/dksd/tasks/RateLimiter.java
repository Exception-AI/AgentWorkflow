package org.dksd.tasks;

import org.dksd.tasks.cache.TaskModel;

import java.util.List;
import java.util.function.BiFunction;

public interface RateLimiter {

    TaskModel call(String parent, String child, List<BiFunction<String, String, TaskModel>> function);
}
