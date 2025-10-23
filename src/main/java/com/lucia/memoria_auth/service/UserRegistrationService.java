package com.lucia.memoria_auth.service;

import com.lucia.memoria_auth.model.User;
import com.lucia.memoria_auth.repository.UserRepository;
import jakarta.validation.ValidationException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public User registerUser(User requestUser) {
    if (userRepository.existsByUsername(requestUser.getUsername()) ||
        userRepository.existsByEmail(requestUser.getEmail())) {

      throw new ValidationException(
          "Username or Email already exists");
    }

    User user = new User();
    user.setUserId(UUID.randomUUID());
    user.setUsername(requestUser.getUsername());
    user.setEmail(requestUser.getEmail());
    user.setPassword(passwordEncoder.encode(requestUser.getPassword()));

    return userRepository.save(user);
  }
}