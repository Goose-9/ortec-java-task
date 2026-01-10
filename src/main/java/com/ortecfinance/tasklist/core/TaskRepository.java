package com.ortecfinance.tasklist.core;

import com.ortecfinance.tasklist.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskRepository {
    void addProject(String name);

    void addTask(String projectName, Task task);

    Optional<List<Task>> findProjectTasks(String projectName);

    Optional<Task> findTaskById(long id);

    Map<String, List<Task>> allProjects();
}
