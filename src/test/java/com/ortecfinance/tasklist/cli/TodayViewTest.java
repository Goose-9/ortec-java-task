package com.ortecfinance.tasklist.cli;

import com.ortecfinance.tasklist.core.InMemoryTaskRepository;
import com.ortecfinance.tasklist.core.TaskListService;
import com.ortecfinance.tasklist.core.TaskRepository;
import org.junit.jupiter.api.*;

import java.io.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class TodayViewTest {
    public static final String PROMPT = "> ";
    private final PipedOutputStream inStream = new PipedOutputStream();
    private final PrintWriter inWriter = new PrintWriter(inStream, true);

    private final PipedInputStream outStream = new PipedInputStream();
    private final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

    private Thread applicationThread;

    public TodayViewTest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new PipedInputStream(inStream)));
        PrintWriter out = new PrintWriter(new PipedOutputStream(outStream), true);

        TaskRepository repo = new InMemoryTaskRepository();
        Clock clock = Clock.fixed(Instant.parse("2021-11-11T00:00:00Z"), ZoneId.of("UTC"));
        TaskListService service = new TaskListService(repo, clock);

        // This constructor must exist (package-private is fine):
        // TaskListCli(BufferedReader, PrintWriter, TaskListService)
        TaskListCli taskList = new TaskListCli(in, out, service);

        applicationThread = new Thread(taskList);
    }

    @BeforeEach
    public void start_the_application() throws IOException {
        applicationThread.start();
        readLines("Welcome to TaskList! Type 'help' for available commands.");
    }

    @AfterEach
    public void kill_the_application() throws IOException, InterruptedException {
        if (!stillRunning()) {
            return;
        }

        Thread.sleep(1000);
        if (!stillRunning()) {
            return;
        }

        applicationThread.interrupt();
        throw new IllegalStateException("The application is still running.");
    }

    @Test
    void it_shows_only_tasks_due_today() throws IOException {
        execute("add project secrets");
        execute("add project training");

        execute("add task secrets Eat more donuts.");
        execute("add task training Refactor the codebase");
        execute("add task training Interaction-Driven Design");

        // fixed today is 11-11-2021
        execute("deadline 1 11-11-2021");
        execute("deadline 2 12-11-2021");
        execute("today");

        readLines(
                "secrets",
                "    [ ] 1: Eat more donuts.",
                ""
        );
        execute("quit");
    }

    private void execute(String command) throws IOException {
        read(PROMPT);
        write(command);
    }

    private void read(String expectedOutput) throws IOException {
        int length = expectedOutput.length();
        char[] buffer = new char[length];
        outReader.read(buffer, 0, length);
        assertThat(String.valueOf(buffer), is(expectedOutput));
    }

    private void readLines(String... expectedOutput) throws IOException {
        for (String line : expectedOutput) {
            read(line + lineSeparator());
        }
    }

    private void write(String input) {
        inWriter.println(input);
    }

    private boolean stillRunning() {
        return applicationThread != null && applicationThread.isAlive();
    }
}

