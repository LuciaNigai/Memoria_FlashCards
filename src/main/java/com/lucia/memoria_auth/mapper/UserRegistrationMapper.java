package com.lucia.memoria_auth.mapper;

import com.lucia.memoria_auth.dto.RegistrationRequestDto;
import com.lucia.memoria_auth.dto.RegistrationResponseDto;
import com.lucia.memoria_auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationMapper {

  public User toEntity(RegistrationRequestDto registrationRequestDto) {
    final var user = new User();

    user.setEmail(registrationRequestDto.email());
    user.setUsername(registrationRequestDto.username());
    user.setPassword(registrationRequestDto.password());

    return user;
  }
  public RegistrationResponseDto toRegistrationResponseDto(
      final User user) {

    return new RegistrationResponseDto(
        user.getEmail(), user.getUsername());
  }
}