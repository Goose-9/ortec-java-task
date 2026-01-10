package com.ortecfinance.tasklist.core;

import com.ortecfinance.tasklist.domain.Task;

import java.util.*;

public class InMemoryTaskRepository implements TaskRepository {
    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();

    @Override
    public void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    @Override
    public void addTask(String projectName, Task task) {
        List<Task> projectTasks = tasks.get(projectName);
        if (projectTasks == null) {
            throw new IllegalArgumentException("Project does not exist: " + projectName);
        }
        projectTasks.add(task);
    }

    @Override
    public Optional<List<Task>> findProjectTasks(String projectName) {
        return Optional.ofNullable(tasks.get(projectName));
    }

    @Override
    public Optional<Task> findTaskById(long id) {
        for (List<Task> projectTasks : tasks.values()) {
            for (Task task : projectTasks) {
                if (task.getId() == id)
                    return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    @Override
    public Map<String, List<Task>> allProjects() {
        return tasks;
    }
}
