package com.ortecfinance.tasklist.rest.dto;

import java.util.List;

public record DeadlineGroupResponse(
    String deadline,                // e.g. "11-11-2021 or null for "No deadline"
    List<ProjectResponse> projects
) {}
