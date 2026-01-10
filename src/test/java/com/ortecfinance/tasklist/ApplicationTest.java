package com.ortecfinance.tasklist;

import org.junit.jupiter.api.*;

import java.io.*;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class ApplicationTest {
    public static final String PROMPT = "> ";
    private final PipedOutputStream inStream = new PipedOutputStream();
    private final PrintWriter inWriter = new PrintWriter(inStream, true);

    private final PipedInputStream outStream = new PipedInputStream();
    private final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

    private Thread applicationThread;

    public ApplicationTest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new PipedInputStream(inStream)));
        PrintWriter out = new PrintWriter(new PipedOutputStream(outStream), true);
        TaskList taskList = new TaskList(in, out);
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
    void it_works() throws IOException {
        execute("show");

        execute("add project secrets");
        execute("add task secrets Eat more donuts.");
        execute("add task secrets Destroy all humans.");

        execute("show");
        readLines(
            "secrets",
            "    [ ] 1: Eat more donuts.",
            "    [ ] 2: Destroy all humans.",
            ""
        );

        execute("add project training");
        execute("add task training Four Elements of Simple Design");
        execute("add task training SOLID");
        execute("add task training Coupling and Cohesion");
        execute("add task training Primitive Obsession");
        execute("add task training Outside-In TDD");
        execute("add task training Interaction-Driven Design");

        execute("check 1");
        execute("check 3");
        execute("check 5");
        execute("check 6");

        execute("show");
        readLines(
                "secrets",
                "    [x] 1: Eat more donuts.",
                "    [ ] 2: Destroy all humans.",
                "",
                "training",
                "    [x] 3: Four Elements of Simple Design",
                "    [ ] 4: SOLID",
                "    [x] 5: Coupling and Cohesion",
                "    [x] 6: Primitive Obsession",
                "    [ ] 7: Outside-In TDD",
                "    [ ] 8: Interaction-Driven Design",
                ""
        );

        execute("quit");
    }

    @Test
    void it_supports_setting_deadlines_and_handles_bad_input() throws IOException {
        execute("add project secrets");
        execute("add task secrets Eat more donuts.");

        // Valid deadline
        execute("deadline 1 25-01-2026");

        // Invalid deadline (catch error and print error message with no crash)
        execute("deadline 1 25-1-2026");
        readLines("Invalid date format. Please use dd-MM-yyyy.");

        // Unknown number ID
        execute("deadline 999 25-01-2026");
        readLines("Could not find a task with an ID of 999.");

        // Non number ID
        execute("deadline one 25-01-2026");
        readLines("Task ID must be a number.");

        execute("quit");
    }

    @Test
    void it_views_tasks_grouped_by_deadline_then_project() throws IOException {
        execute("add project Secrets");
        execute("add project Training");
        execute("add task Secrets Eat more donuts.");
        execute("add task Training Refactor the codebase");
        execute("add task Training Interaction-Driven Design");
        execute("add task Training Four Elements of Simple Design");

        execute("deadline 1 11-11-2021");
        execute("deadline 4 11-11-2021");
        execute("deadline 3 13-11-2021");

        execute("view-by-deadline");

        readLines(
                "11-11-2021:",
                        "     Secrets:",
                        "          1: Eat more donuts.",
                        "     Training:",
                        "          4: Four Elements of Simple Design",
                        "",
                        "13-11-2021:",
                        "     Training:",
                        "          3: Interaction-Driven Design",
                        "",
                        "No deadline:",
                        "     Training:",
                        "          2: Refactor the codebase",
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
