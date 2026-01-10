package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.domain.Task;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void deadline_is_optional_and_empty_by_default(){
        Task task = new Task(1, "Demo", false);
        assertTrue(task.getDeadline().isEmpty());
    }

    @Test
    void deadline_is_set(){
        Task task = new Task(2, "Demo", false);
        task.setDeadline(LocalDate.of(2026,1,10));

        assertTrue(task.getDeadline().isPresent());
        assertEquals(LocalDate.of(2026,1,10), task.getDeadline().get());
    }
}
