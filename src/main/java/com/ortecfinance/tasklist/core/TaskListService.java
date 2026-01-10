package com.ortecfinance.tasklist.core;

import com.ortecfinance.tasklist.Task;

import java.time.LocalDate;
import java.util.*;

public final class TaskListService {
    private final TaskRepository repository;
    private long lastId = 0;

    public TaskListService(TaskRepository repository) {
        this.repository = repository;
    }

    public Map<String, List<Task>> allProjects() {
        return repository.allProjects();
    }

    public void addProject(String name) {
        repository.addProject(name);
    }

    /**
     * @return true if the project exists and the task was added, else false
     */
    public boolean addTask(String projectName, String description) {
        if (repository.findProjectTasks(projectName).isEmpty()) return false;
        repository.addTask(projectName, new Task(nextId(), description, false));
        return true;
    }

    /**
     * @return true if the task exists, else false
     */
    public boolean setDone(long taskId, boolean done) {
        return updateTask(taskId, task -> task.setDone(done));
    }

    /**
     * @return true if the task exists, else false
     */
    public boolean setDeadline(long taskId, LocalDate deadline) {
        return updateTask(taskId, task -> task.setDeadline(deadline));
    }

    public record DeadlineGroups(
            Map<LocalDate, Map<String, List<Task>>> byDeadline,
            Map<String, List<Task>> noDeadline
    ) {}

    public DeadlineGroups viewByDeadlineGroups() {
        Map<LocalDate, Map<String, List<Task>>> byDeadline = new TreeMap<>();
        Map<String,List<Task>> noDeadline = new TreeMap<>();

        for (Map.Entry<String, List<Task>> project : repository.allProjects().entrySet()) {
            String projectName = project.getKey();
            for (Task task : project.getValue()) {
                task.getDeadline().ifPresentOrElse(
                        deadline -> byDeadline
                                .computeIfAbsent(deadline, t -> new TreeMap<>())
                                .computeIfAbsent(projectName, l -> new ArrayList<>())
                                .add(task),
                        () -> noDeadline
                                .computeIfAbsent(projectName, l -> new ArrayList<>())
                                .add(task)
                );
            }
        }

        return new DeadlineGroups(byDeadline, noDeadline);
    }

    private boolean updateTask(long taskId, java.util.function.Consumer<Task> updater) {
        Optional<Task> taskOpt = repository.findTaskById(taskId);
        if (taskOpt.isEmpty()) return false;
        updater.accept(taskOpt.get());
        return true;
    }

    private long nextId() {
        return ++lastId;
    }
}
