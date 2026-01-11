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
}
