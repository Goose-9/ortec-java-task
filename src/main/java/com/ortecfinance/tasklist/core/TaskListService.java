package com.ortecfinance.tasklist.core;

import com.ortecfinance.tasklist.domain.Task;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

public final class TaskListService {
    private final TaskRepository repository;
    private final Clock clock;
    private long lastId = 0;

    public TaskListService(TaskRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    public TaskListService(TaskRepository repository, Clock clock) {

        this.repository = repository;
        this.clock = clock;
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
        return createTask(projectName, description).isPresent();
    }

    public Optional<Task> createTask(String projectName, String description) {
        if (repository.findProjectTasks(projectName).isEmpty()) return Optional.empty();
        Task task = new Task(nextId(),description,false);
        repository.addTask(projectName, task);
        return Optional.of(task);
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

    public Map<String, List<Task>> tasksDueToday() {
        LocalDate today = today();
        Map<String, List<Task>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<Task>> project : repository.allProjects().entrySet()) {
            List<Task> dueToday = project.getValue().stream()
                    .filter(t -> t.getDeadline().isPresent() && t.getDeadline().get().equals(today))
                    .toList();

            if (!dueToday.isEmpty()) {
                result.put(project.getKey(), new ArrayList<>(dueToday));
            }
        }
        return result;
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

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
