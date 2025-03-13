package org.dksd.tasks.cache;

import org.dksd.tasks.Identifier;
import org.dksd.tasks.NameIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Cache<T extends Identifier & NameIdentifier> {

    private final Map<UUID, T> map = new HashMap<>();
    private final Map<String, T> nameMap = new HashMap<>();
    private final List<T> allEntries;

    public Cache(List<T> allEntries) {
        this.allEntries = allEntries;
    }

    public void add(T entry) {
        map.put(entry.getId(), entry);
        nameMap.put(entry.getName(), entry);
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

    public T get(String name) {
        if (!nameMap.containsKey(name)) {
            for (T allEntry : allEntries) {
                if (allEntry.getName().equals(name)) {
                    nameMap.put(name, allEntry);
                    return allEntry;
                }
            }
        }
        return nameMap.get(name);
    }

}
