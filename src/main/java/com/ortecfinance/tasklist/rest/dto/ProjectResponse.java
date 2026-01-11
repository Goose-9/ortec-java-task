package com.ortecfinance.tasklist.rest.dto;

import java.util.List;

public record ProjectResponse(String name, List<TaskResponse> tasks) {}
