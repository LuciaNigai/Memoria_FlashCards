package com.lucia.memoria_auth.dto;

public record AuthenticationRequestDto(
    String username,
    String password
) {
}
