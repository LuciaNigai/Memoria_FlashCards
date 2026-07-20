package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.DeckRequestDTO;
import com.lucia.memoria.dto.local.TemplateFieldRequestDTO;
import com.lucia.memoria.dto.local.TemplateRequestDTO;
import com.lucia.memoria.dto.local.UserDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.mapper.UserMapper;
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
  private final DeckService deckService;
  private final TemplateService templateService;

  @Transactional
  public UserDTO createUser(UserDTO userDTO) {
    User user = userMapper.toEntity(userDTO);
    user.setUserId(UUID.randomUUID());

    User savedUser = userRepository.save(user);

    // 1. Create Default Deck
    createDefaultDeck(savedUser.getUserId());

    // 2. Create Default Template with 4 fields
    createDefaultTemplate(savedUser.getUserId());

    return userMapper.toDTO(savedUser);
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

  private void createDefaultDeck(UUID userId) {
    DeckRequestDTO defaultDeckDTO = new DeckRequestDTO();
    defaultDeckDTO.setName("Default");
    defaultDeckDTO.setUserId(userId);
    defaultDeckDTO.setPath(null);

    deckService.createDeck(defaultDeckDTO);
  }

  private void createDefaultTemplate(UUID ownerId) {
    TemplateFieldRequestDTO wordField = new TemplateFieldRequestDTO(null, "word", FieldRole.FRONT);
    TemplateFieldRequestDTO transcriptionField = new TemplateFieldRequestDTO(null, "transcription", FieldRole.FRONT);
    TemplateFieldRequestDTO posField = new TemplateFieldRequestDTO(null, "partOfSpeech", FieldRole.AUXILIARY);
    TemplateFieldRequestDTO definitionField = new TemplateFieldRequestDTO(null, "definition", FieldRole.BACK);
    TemplateFieldRequestDTO exampleField = new TemplateFieldRequestDTO(null, "example", FieldRole.AUXILIARY);

    TemplateRequestDTO defaultTemplateDTO = new TemplateRequestDTO();
    defaultTemplateDTO.setName("Default");
    defaultTemplateDTO.setOwnerId(ownerId);
    defaultTemplateDTO.setFields(List.of(
        wordField,
        transcriptionField,
        posField,
        definitionField,
        exampleField
    ));

    templateService.createTemplate(defaultTemplateDTO);
  }
}
