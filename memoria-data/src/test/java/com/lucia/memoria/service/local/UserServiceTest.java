package com.lucia.memoria.service.local;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lucia.memoria.dto.local.UserDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.mapper.UserMapper;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.DeckRepository;
import com.lucia.memoria.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private DeckRepository deckRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserService userService;

  private UUID userId;
  private User user;
  private UserDTO userDTO;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = new User();
    user.setUserId(userId);
    user.setUsername("testuser");

    userDTO = new UserDTO();
    userDTO.setId(userId);
    userDTO.setUsername("testuser");
  }

  @Test
  @DisplayName("Should successfully create a new user")
  void createUser_success() {
    // Arrange
    when(userMapper.toEntity(any(UserDTO.class))).thenReturn(user);
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

    // Act
    UserDTO result = userService.createUser(userDTO);

    // Assert
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Should successfully retrieve a user by ID")
  void getUserById_success() {
    // Arrange
    when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    // Act
    UserDTO result = userService.getUserById(userId);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getId());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when user ID is not found")
  void getUserById_notFound_throwsException() {
    // Arrange
    when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> userService.getUserById(userId));
  }

  @Test
  @DisplayName("Should successfully retrieve all users")
  void getAllUsers_success() {
    // Arrange
    when(userRepository.findAll()).thenReturn(List.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    // Act
    List<UserDTO> result = userService.getAllUsers();

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should successfully delete a user when they have no decks")
  void deleteUser_success_noDecks() {
    // Arrange
    when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
    when(deckRepository.existsByUser(user)).thenReturn(false);

    // Act
    userService.deleteUser(userId, false);

    // Assert
    verify(userRepository).delete(user);
  }

  @Test
  @DisplayName("Should throw ConflictWithDataException when deleting user with decks without force flag")
  void deleteUser_withDecks_noForce_throwsConflict() {
    // Arrange
    when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
    when(deckRepository.existsByUser(user)).thenReturn(true);

    // Act & Assert
    assertThrows(ConflictWithDataException.class, () -> userService.deleteUser(userId, false));
    verify(userRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should successfully delete user even with decks when force flag is true")
  void deleteUser_withDecks_forceTrue_success() {
    // Arrange
    when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

    // Act
    userService.deleteUser(userId, true);

    // Assert
    verify(userRepository).delete(user);
    // deckRepository.existsByUser is not even called if force is true
  }
}
