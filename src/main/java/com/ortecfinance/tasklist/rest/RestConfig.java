package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.core.InMemoryTaskRepository;
import com.ortecfinance.tasklist.core.TaskListService;
import com.ortecfinance.tasklist.core.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RestConfig {
    @Bean
    public TaskRepository taskRepository() {
        return new InMemoryTaskRepository();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public TaskListService taskListService(TaskRepository repo, Clock clock){
        return new TaskListService(repo, clock);
    }
}
