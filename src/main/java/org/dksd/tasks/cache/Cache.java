package org.dksd.tasks.cache;

import org.dksd.tasks.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Cache<T extends Identifier> {

    private final Map<UUID, T> map = new HashMap<>();
    private final List<T> allEntries;

    public Cache(List<T> allEntries) {
        this.allEntries = allEntries;
    }

    public void add(T entry) {
        map.put(entry.getId(), entry);
    }

    public T get(UUID id) {
        if (!map.containsKey(id)) {
            for (T allEntry : allEntries) {
                if (allEntry.getId().equals(id)) {
                    map.put(id, allEntry);
                    return allEntry;
                }
            }
        }
        return map.get(id);
    }

}
