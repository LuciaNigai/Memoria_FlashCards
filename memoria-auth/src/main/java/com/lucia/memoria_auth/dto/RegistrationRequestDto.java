package com.lucia.memoria_auth.dto;

public record RegistrationRequestDto(
    String username,
    String email,
    String password
) {
}