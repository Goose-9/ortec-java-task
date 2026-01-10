package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.cli.TaskListCli;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskListApplication {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Starting console Application");
            TaskListCli.startConsole();
        }
        else {
            SpringApplication.run(TaskListApplication.class, args);
            System.out.println("localhost:8080/tasks");
        }
    }

}
