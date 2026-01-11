package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.domain.DateFormats;
import com.ortecfinance.tasklist.domain.Task;
import com.ortecfinance.tasklist.core.TaskListService;
import com.ortecfinance.tasklist.rest.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.rest.dto.ProjectResponse;
import com.ortecfinance.tasklist.rest.dto.TaskResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private TaskResponse toTaskResponse(Task task) {
        String deadline = task.getDeadline()
                .map(d -> d.format(DateFormats.DEADLINE_FORMAT))
                .orElse(null);

        return new TaskResponse(task.getId(), task.getDescription(), task.isDone(), deadline);
    }
}
