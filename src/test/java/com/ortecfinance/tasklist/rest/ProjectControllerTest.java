package com.ortecfinance.tasklist.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

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
        mvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void get_projects_returns_projects_with_tasks() throws Exception {
        mvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("secrets"))
                .andExpect(jsonPath("$[0].tasks").isArray());
    }

    @Test
    void post_projects_rejects_blank_name() throws Exception {
        mvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_projects_tasks_creates_task() throws Exception {
        mvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/projects/secrets/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\":\"Eat more donuts.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Eat more donuts."))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.deadline").isEmpty());
    }

    @Test
    void post_project_tasks_unknown_project_returns_404() throws Exception {
        mvc.perform(post("/projects/doesnotexist/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_project_tasks_blank_description_returns_400() throws Exception {
        mvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/projects/secrets/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_project_task_deadline_updates_deadline() throws Exception {
        mvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/projects/secrets/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\":\"Eat more donuts.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        // Task id will be 1 since each test is a fresh context (because of @DirtiesContext,
        // can be changed to use the response from the task creation request above.
        mvc.perform(put("/projects/secrets/tasks/1")
                        .param("deadline", "25-01-2026"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tasks[0].deadline").value("25-01-2026"));
    }

    @Test
    void put_project_task_deadline_wrong_project_returns_404() throws Exception {
        mvc.perform(post("/projects").contentType(APPLICATION_JSON).content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/projects").contentType(APPLICATION_JSON).content("{\"name\":\"training\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/projects/secrets/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\":\"Eat more donuts.\"}"))
                .andExpect(status().isCreated());

        // Try and update the task under project "training", but the task is under "secrets".
        mvc.perform(put("/projects/training/tasks/1")
                        .param("deadline", "25-01-2026"))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_project_task_deadline_bad_format_returns_400() throws Exception {
        mvc.perform(post("/projects").contentType(APPLICATION_JSON).content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/projects/secrets/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\":\"Eat more donuts.\"}"))
                .andExpect(status().isCreated());

        mvc.perform(put("/projects/training/tasks/1")
                        .param("deadline", "25-1-2026"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_project_task_deadline_unknown_task_returns_404() throws Exception {
        mvc.perform(post("/projects").contentType(APPLICATION_JSON).content("{\"name\":\"secrets\"}"))
                .andExpect(status().isCreated());

        mvc.perform(put("/projects/training/tasks/999")
                        .param("deadline", "25-01-2026"))
                .andExpect(status().isNotFound());
    }
}
