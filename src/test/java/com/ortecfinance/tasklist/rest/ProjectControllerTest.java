package com.ortecfinance.tasklist.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProjectControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void post_projects_creates_project() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());
    }

    @Test
    void get_projects_returns_projects_with_tasks() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());
        getProjects()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Secrets"))
                .andExpect(jsonPath("$[0].tasks").isArray());
    }

    @Test
    void post_projects_rejects_blank_name() throws Exception {
        postJson("/projects", "{\"name\":\"   \"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_projects_tasks_creates_task() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());

        createTask("Secrets", "Eat more donuts.")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Eat more donuts."))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.deadline").isEmpty());
    }

    @Test
    void post_project_tasks_unknown_project_returns_404() throws Exception {
        createTask("doesnotexist", "X")
                .andExpect(status().isNotFound());
    }

    @Test
    void post_project_tasks_blank_description_returns_400() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());

        postJson("/projects/Secrets/tasks", "{\"description\":\"  \"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_project_task_deadline_updates_deadline() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());

        createTask("Secrets", "Eat more donuts.")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        // Task id will be 1 since each test is a fresh context (because of @DirtiesContext,
        // can be changed to use the response from the task creation request above.
        setDeadline("Secrets",1, "25-01-2026")
                .andExpect(status().isNoContent());

        getProjects()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tasks[0].deadline").value("25-01-2026"));
    }

    @Test
    void put_project_task_deadline_wrong_project_returns_404() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());
        createProject("Training").andExpect(status().isCreated());
        createTask("Secrets", "Eat more donuts.").andExpect(status().isCreated());

        // Try and update the task under project "Training", but the task is under "Secrets".
        setDeadline("Training", 1, "25-01-2026")
                .andExpect(status().isNotFound());
    }

    @Test
    void put_project_task_deadline_bad_format_returns_400() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());
        createTask("Secrets", "Eat more donuts.").andExpect(status().isCreated());

        // invalid date format (should be dd-MM-yyyy)
        setDeadline("Training", 1, "25-1-2026")
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_project_task_deadline_unknown_task_returns_404() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());

        setDeadline("Secrets", 999, "25-01-2026")
                .andExpect(status().isNotFound());
    }

    @Test
    void get_view_by_deadline_groups_tasks_by_deadline() throws Exception {
        createProject("Secrets").andExpect(status().isCreated());
        createProject("Training").andExpect(status().isCreated());

        createTask("Secrets", "Eat more donuts.").andExpect(status().isCreated());
        createTask("Training", "Refactor the codebase").andExpect(status().isCreated());

        setDeadline("Secrets", 1, "11-11-2021").andExpect(status().isNoContent());

        getViewByDeadline()
                .andExpect(status().isOk())
                // first group is 11-11-2021
                .andExpect(jsonPath("$[0].deadline").value("11-11-2021"))
                .andExpect(jsonPath("$[0].projects[0].name").value("Secrets"))
                .andExpect(jsonPath("$[0].projects[0].tasks[0].description").value("Eat more donuts."))
                // last group is no-deadline
                .andExpect(jsonPath("$[1].deadline").isEmpty());
    }

    @Test
    void get_view_by_deadline_empty_returns_empty_array() throws Exception {
        getViewByDeadline()
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -------------------------
    // Helpers
    // -------------------------

    private ResultActions createProject(String name) throws Exception {
        return postJson("/projects", "{\"name\":\"" + escapeJson(name) + "\"}");
    }

    private ResultActions createTask(String project, String description) throws Exception {
        return postJson("/projects/" + project + "/tasks", "{\"description\":\"" + escapeJson(description) + "\"}");
    }

    private ResultActions setDeadline(String project, long taskId, String deadline) throws Exception {
        return mvc.perform(put("/projects/" + project + "/tasks/" + taskId)
                .param("deadline", deadline));
    }

    private ResultActions getProjects() throws Exception {
        return mvc.perform(get("/projects"));
    }

    private ResultActions getViewByDeadline() throws Exception {
        return mvc.perform(get("/projects/view_by_deadline"));
    }

    private ResultActions postJson(String path, String jsonBody) throws Exception {
        return mvc.perform(post(path)
                .contentType(APPLICATION_JSON)
                .content(jsonBody));
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
