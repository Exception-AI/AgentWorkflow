package org.example;

import java.util.List;

public class Creator {
    public static List<Task> of(Task...tasks) {
        return List.of(tasks);
    }

}
