package ru.vovandiya.dto;

public record UserRequest(
        String username,
        String password,
        String role
) {}
