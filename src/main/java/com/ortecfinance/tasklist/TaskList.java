package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Array;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private final BufferedReader in;
    private final PrintWriter out;

    private long lastId = 0;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskList(in, out).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        this.in = reader;
        this.out = writer;
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
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                out.printf("    [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
            }
            out.println();
        }
    }

    private void viewByDeadline(){
        Map<LocalDate, Map<String, List<Task>>> byDeadline = new TreeMap<>();
        Map<String,List<Task>> noDeadline = new TreeMap<>();

        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            String projectName = project.getKey();

            for (Task task : project.getValue()) {
                Optional<LocalDate> deadlineOpt = task.getDeadline();
                if (deadlineOpt.isPresent()) {
                    LocalDate deadline = deadlineOpt.get();
                    Map<String, List<Task>> byProject =
                            byDeadline.computeIfAbsent(deadline, t -> new TreeMap<>());
                    byProject.computeIfAbsent(projectName, l -> new ArrayList<>()).add(task);
                } else {
                    noDeadline.computeIfAbsent(projectName, l -> new ArrayList<>()).add(task);
                }
            }
        }

        // print deadline groups
        for (Map.Entry<LocalDate, Map<String, List<Task>>> dateGroup : byDeadline.entrySet()) {
            out.println(dateGroup.getKey().format(DateFormats.DEADLINE_FORMAT) + ":");

            for (Map.Entry<String, List<Task>> projectGroup : dateGroup.getValue().entrySet()) {
                out.println("     " + projectGroup.getKey() + ":");

                projectGroup.getValue().sort(Comparator.comparingLong(Task::getId));
                for (Task task : projectGroup.getValue()) {
                    out.printf("          %d: %s%n", task.getId(), task.getDescription());
                }
            }
            out.println();
        }

        // print no deadline groups
        if (!noDeadline.isEmpty()) {
            out.println("No deadline:");

            for (Map.Entry<String, List<Task>> projectGroup : noDeadline.entrySet()) {
                out.println("     " + projectGroup.getKey() + ":");

                projectGroup.getValue().sort(Comparator.comparingLong(Task::getId));
                for (Task task : projectGroup.getValue()) {
                    out.printf("          %d: %s%n", task.getId(), task.getDescription());
                }
            }
            out.println();
        }
    }

    private void deadline(String commandLine) {
        String[] deadlineCommandRest = commandLine.split(" ", 2);
        int id;

        try{
            id = Integer.parseInt(deadlineCommandRest[0]);
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

        Optional<Task> taskOpt = findTaskById(id);
        if (taskOpt.isPresent()) {
            taskOpt.get().setDeadline(date);
            return;
        }

        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
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
        tasks.put(name, new ArrayList<Task>());
    }

    private void addTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);
        if (projectTasks == null) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
            return;
        }
        projectTasks.add(new Task(nextId(), description, false));
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        int id = Integer.parseInt(idString);

        Optional<Task> taskOpt = findTaskById(id);
        if (taskOpt.isPresent()) {
            taskOpt.get().setDone(done);
            return;
        }

        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
    }

    private Optional<Task> findTaskById(int id) {
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    return Optional.of(task);
                }
            }
        }
        return Optional.empty();
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

    private long nextId() {
        return ++lastId;
    }
}
