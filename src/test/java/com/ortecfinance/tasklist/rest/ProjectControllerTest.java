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
}
