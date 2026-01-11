package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.domain.DateFormats;
import com.ortecfinance.tasklist.domain.Task;
import com.ortecfinance.tasklist.core.TaskListService;
import com.ortecfinance.tasklist.rest.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final TaskListService service;

    public ProjectController (TaskListService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> createProject(@RequestBody CreateProjectRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()){
            return ResponseEntity.badRequest().build();
        }

        service.addProject(request.name().trim());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<ProjectResponse> getProjects() {
        Map<String, List<Task>> projects = service.allProjects();

        return projects.entrySet().stream()
                .map(entry -> new ProjectResponse(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(this::toTaskResponse)
                                .toList()
                ))
                .toList();
    }

    @PostMapping("/{project}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable("project") String project,
            @RequestBody CreateTaskRequest request
            ) {
        if (request == null || request.description() == null || request.description().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return service.createTask(project, request.description().trim())
                .map(task -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(toTaskResponse(task)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{project}/tasks/{taskId}")
    public ResponseEntity<Void> updateDeadline(
            @PathVariable String project,
            @PathVariable long taskId,
            @RequestParam("deadline") String deadline
    ) {
        if (deadline == null || deadline.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        final LocalDate parsed;
        try {
            parsed = LocalDate.parse(deadline, DateFormats.DEADLINE_FORMAT);
        } catch (DateTimeParseException e){
            return ResponseEntity.badRequest().build();
        }

        boolean updated = service.setDeadline(project, taskId, parsed);
        if (!updated) {
            // either project does not exist or task not in specified project
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/view_by_deadline")
    public List<DeadlineGroupResponse> viewByDeadline(){
        TaskListService.DeadlineGroups groups = service.viewByDeadlineGroups();

        List<DeadlineGroupResponse> result = new ArrayList<>();

        for (var dataEntry : groups.byDeadline().entrySet()) {
            String dateString = dataEntry.getKey().format(DateFormats.DEADLINE_FORMAT);
            result.add(new DeadlineGroupResponse(dateString, toProjectResponses(dataEntry.getValue())));
        }

        if (!groups.noDeadline().isEmpty()) {
            result.add(new DeadlineGroupResponse(null, toProjectResponses(groups.noDeadline())));
        }

        return result;
    }

    private List<ProjectResponse> toProjectResponses(Map<String, List<Task>> byProject) {
        return byProject.entrySet().stream()
                .map(entry -> new ProjectResponse(
                        entry.getKey(),
                        entry.getValue().stream().map(this::toTaskResponse).toList()
                ))
                .toList();
    }

    private TaskResponse toTaskResponse(Task task) {
        String deadline = task.getDeadline()
                .map(d -> d.format(DateFormats.DEADLINE_FORMAT))
                .orElse(null);

        return new TaskResponse(task.getId(), task.getDescription(), task.isDone(), deadline);
    }
}
