package org.dksd.tasks;

import java.time.Instant;
import java.util.*;

public class Task implements Identifier {

    private UUID id;
    private String name;
    private String description;
    private Map<String, Object> metadata = new HashMap<>();
    private Long createdTime;
    private Long lastModifiedTime;

    public Task() {
        this.id = UUID.randomUUID();
        this.createdTime = Instant.now().getEpochSecond();
        this.lastModifiedTime = Instant.now().getEpochSecond();
    }

    public Task(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.createdTime = Instant.now().getEpochSecond();
        this.lastModifiedTime = Instant.now().getEpochSecond();
    }

    public Task(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdTime = Instant.now().getEpochSecond();
        this.lastModifiedTime = Instant.now().getEpochSecond();
    }

    public void updateTask(String ename, String edesc) {
        setName(ename);
        setDescription(edesc);
        setLastModifiedTime(Instant.now().getEpochSecond());
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public Long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdTime=" + createdTime +
                ", lastModifiedTime=" + lastModifiedTime +
                '}';
    }

    public UUID getId() {
        return id;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return Objects.equals(name, task.name) && Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
}
