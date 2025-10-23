package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.UserDTO;
import com.lucia.memoria.mapper.UserMapper;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Transactional
  public UserDTO createUser(UserDTO userDTO) {
    User user = userMapper.toEntity(userDTO);
    user.setUserId(UUID.randomUUID());

    User saved = userRepository.save(user);
    return userMapper.toDTO(saved);
  }

  @Transactional(readOnly = true)
  public UserDTO getUserById(UUID userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return userMapper.toDTO(user);
  }

  @Transactional(readOnly = true)
  public User getUserEntityById(UUID userId) {
    return userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  @Transactional(readOnly = true)
  public List<UserDTO> getAllUsers() {
    return userRepository.findAll().stream()
        .map(userMapper::toDTO)
        .collect(Collectors.toList());
  }
}
