package org.dksd.tasks.cache;

import java.util.List;
import java.util.function.BiFunction;

public class FallbackRateLimiter implements RateLimiter {

    @Override
    public TaskModel call(String parent, String child, List<BiFunction<String, String, TaskModel>> functions) {
        if (functions.isEmpty()) {
            throw new RuntimeException("List of function cannot be empty!");
        }
        BiFunction<String, String, TaskModel> func = functions.removeFirst();
        try {
            return func.apply(parent, child);
        } catch (Exception ep) {
            return call(parent, child, functions);
        }
    }
}
