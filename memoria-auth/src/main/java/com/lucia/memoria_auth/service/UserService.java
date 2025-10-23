package com.lucia.memoria_auth.service;

import static org.springframework.http.HttpStatus.GONE;

import com.lucia.memoria_auth.model.User;
import com.lucia.memoria_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getUserByUsername(final String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(GONE,
            "The user account has been deleted or inactivated"));
  }
}
