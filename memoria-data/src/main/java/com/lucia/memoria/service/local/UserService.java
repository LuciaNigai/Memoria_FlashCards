package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.DeckResponseDTO;
import com.lucia.memoria.dto.local.UserDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.mapper.UserMapper;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.DeckRepository;
import com.lucia.memoria.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final DeckRepository deckRepository;
  private final UserMapper userMapper;

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
        .toList();
  }

  // TODO: when I will move to extracting user from JWT will have to handle deck deletion manually
  public void deleteUser(UUID userId, boolean force) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!force && deckRepository.existsByUser(user)) {
      throw new ConflictWithDataException("Cannot delete user with decks without force flag.");
    }

    userRepository.delete(user);
  }
}
