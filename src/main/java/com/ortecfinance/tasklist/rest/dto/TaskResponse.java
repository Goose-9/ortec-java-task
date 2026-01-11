package com.ortecfinance.tasklist.rest.dto;

public record TaskResponse(long id, String description, boolean done, String deadline) {}
