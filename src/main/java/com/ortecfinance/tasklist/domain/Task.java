package com.ortecfinance.tasklist.domain;

import java.time.LocalDate;
import java.util.Optional;

public final class Task {
    private final long id;
    private final String description;
    private boolean done;
    private LocalDate deadline;

    public Task(long id, String description, boolean done) {
        this.id = id;
        this.description = description;
        this.done = done;
        this.deadline = null;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Optional<LocalDate> getDeadline() {
        return Optional.ofNullable(deadline);
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
