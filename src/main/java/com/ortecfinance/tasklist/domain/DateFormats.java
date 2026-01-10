package com.ortecfinance.tasklist.domain;

import java.time.format.DateTimeFormatter;

public class DateFormats {
    private DateFormats() {}

    public static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");
}
