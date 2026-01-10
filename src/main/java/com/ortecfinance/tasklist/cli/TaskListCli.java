package com.ortecfinance.tasklist.cli;

import com.ortecfinance.tasklist.core.InMemoryTaskRepository;
import com.ortecfinance.tasklist.core.TaskListService;
import com.ortecfinance.tasklist.domain.DateFormats;
import com.ortecfinance.tasklist.domain.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class TaskListCli implements Runnable {
    private static final String QUIT = "quit";

    private final BufferedReader in;
    private final PrintWriter out;
    private final TaskListService service;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskListCli(in, out).run();
    }

    public TaskListCli(BufferedReader reader, PrintWriter writer) {
        this.in = reader;
        this.out = writer;
        this.service = new TaskListService(new InMemoryTaskRepository());
    }

    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "show":
                show();
                break;
            case "deadline":
                deadline(commandRest[1]);
                break;
            case "view-by-deadline":
                viewByDeadline();
                break;
            case "add":
                add(commandRest[1]);
                break;
            case "check":
                check(commandRest[1]);
                break;
            case "uncheck":
                uncheck(commandRest[1]);
                break;
            case "help":
                help();
                break;
            default:
                error(command);
                break;
        }
    }

    private void show() {
        for (Map.Entry<String, List<Task>> project : service.allProjects().entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                out.printf("    [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
            }
            out.println();
        }
    }

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String name) {
        service.addProject(name);
    }

    private void addTask(String project, String description) {
        boolean ok = service.addTask(project, description);
        if (!ok) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
        }
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        int id = Integer.parseInt(idString);

        boolean ok = service.setDone(id, done);
        if (!ok) {
            out.printf("Could not find a task with an ID of %d.", id);
            out.println();
        }
    }

    private static final String INDENT_PROJECT = "     ";       // 5 spaces
    private static final String INDENT_TASK = "          ";     // 10 spaces

    private void viewByDeadline(){
        TaskListService.DeadlineGroups groups = service.viewByDeadlineGroups();
        printDeadlineGroups(groups.byDeadline());
        printNoDeadlineGroups(groups.noDeadline());
    }

    private void printDeadlineGroups(Map<LocalDate,Map<String, List<Task>>> byDeadline) {
        for (Map.Entry<LocalDate, Map<String, List<Task>>> dateGroup : byDeadline.entrySet()) {
            out.println(dateGroup.getKey().format(DateFormats.DEADLINE_FORMAT) + ":");
            printProjects(dateGroup.getValue());
            out.println();
        }
    }

    private void printNoDeadlineGroups(Map<String, List<Task>> noDeadline) {
        if (noDeadline.isEmpty()) return;

        out.println("No deadline:");
        printProjects(noDeadline);
        out.println();
    }

    private void printProjects(Map<String, List<Task>> byProject) {
        for (Map.Entry<String, List<Task>> projectGroup : byProject.entrySet()) {
            out.println(INDENT_PROJECT + projectGroup.getKey() + ":");
            printTasks(projectGroup.getValue());
        }
    }

    private void printTasks(List<Task> tasks) {
        tasks.sort(Comparator.comparingLong(Task::getId));
        for (Task task : tasks) {
            out.printf(INDENT_TASK + "%d: %s%n", task.getId(), task.getDescription());
        }
    }

    private void deadline(String commandLine) {
        String[] deadlineCommandRest = commandLine.split(" ", 2);
        long id;

        try{
            id = Long.parseLong(deadlineCommandRest[0]);
        } catch (NumberFormatException e) {
            out.println("Task ID must be a number.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(deadlineCommandRest[1], DateFormats.DEADLINE_FORMAT);
        } catch (DateTimeParseException e) {
            out.println("Invalid date format. Please use dd-MM-yyyy.");
            return;
        }

        setDeadline(id, date);
    }

    private void setDeadline(Long taskId, LocalDate deadline) {
        boolean ok = service.setDeadline(taskId, deadline);
        if (!ok) {
            out.printf("Could not find a task with an ID of %d.", taskId);
            out.println();
        }
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <date>");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }
}
