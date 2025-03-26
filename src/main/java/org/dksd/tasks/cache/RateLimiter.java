package org.dksd.tasks.cache;

import java.util.List;
import java.util.function.BiFunction;

public interface RateLimiter {

    TaskModel call(String parent, String child, List<BiFunction<String, String, TaskModel>> function);
}
