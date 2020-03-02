package org.drw.model;

import java.util.UUID;

public class Task {

    public String id;
    public String description;
    public boolean completed;

    public Task(String description){
        id = UUID.randomUUID().toString();
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
