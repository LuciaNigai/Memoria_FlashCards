package com.lucia.memoria.service.local;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lucia.memoria.dto.local.TagRequestDTO;
import com.lucia.memoria.dto.local.TagResponseDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.DuplicateException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.mapper.TagMapper;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Tag;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.TagRepository;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Unit Tests")
class TagServiceTest {

  @Mock
  private TagRepository tagRepository;

  @Mock
  private TagMapper tagMapper;

  @Mock
  private UserService userService;

  @InjectMocks
  private TagService tagService;

  private UUID userId;
  private User user;
  private UUID tagId;
  private Tag tag;
  private TagResponseDTO tagResponseDTO;
  private TagRequestDTO tagRequestDTO;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = new User();
    user.setUserId(userId);

    tagId = UUID.randomUUID();
    tag = new Tag();
    tag.setTagId(tagId);
    tag.setName("Test Tag");
    tag.setColor("#FFFFFF");
    tag.setUser(user);

    tagRequestDTO = new TagRequestDTO(tagId, "Test Tag", "#FFFFFF");
    tagResponseDTO = new TagResponseDTO(tagId, "Test Tag", "#FFFFFF", OffsetDateTime.now(), OffsetDateTime.now());
  }

  // ==========================================
  // Tests for createTag(UUID userId, TagDTO tagDTO)
  // ==========================================

  @Test
  @DisplayName("Should successfully create a new tag")
  void createTag_success() {
    // Arrange
    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenReturn(tag);
    when(tagMapper.toDTO(any(Tag.class))).thenReturn(tagResponseDTO);

    // Act
    TagResponseDTO result = tagService.createTag(userId, tagRequestDTO);

    // Assert
    assertNotNull(result);
    assertEquals(tagResponseDTO.name(), result.name());
    verify(userService).getUserEntityById(userId);
    verify(tagRepository).findByName("Test Tag");
    verify(tagRepository).save(any(Tag.class));
    verify(tagMapper).toDTO(any(Tag.class));
  }

  @Test
  @DisplayName("Should throw DuplicateException when creating a tag with an existing name")
  void createTag_duplicateName_throwsDuplicateException() {
    // Arrange
    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(tagRepository.findByName(anyString())).thenReturn(Optional.of(tag));

    // Act & Assert
    DuplicateException thrown = assertThrows(DuplicateException.class,
        () -> tagService.createTag(userId, tagRequestDTO));

    assertEquals("The tag with that name already exists", thrown.getMessage());
    verify(userService).getUserEntityById(userId);
    verify(tagRepository).findByName("Test Tag");
    verify(tagRepository, never()).save(any(Tag.class));
  }

  // ==========================================
  // Tests for getAllUserTags(UUID userId)
  // ==========================================

  @Test
  @DisplayName("Should successfully retrieve all tags for a user")
  void getAllUserTags_success() {
    // Arrange
    List<Tag> tags = Collections.singletonList(tag);

    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(tagRepository.findByUser(user)).thenReturn(tags);
    when(tagMapper.toDTOList(tags)).thenReturn(List.of(tagResponseDTO));

    // Act
    List<TagResponseDTO> result = tagService.getAllUserTags(userId);

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(tagResponseDTO.name(), result.getFirst().name());
    verify(userService).getUserEntityById(userId);
    verify(tagRepository).findByUser(user);
    verify(tagMapper).toDTOList(tags);
  }

  // ==========================================
  // Tests for deleteTag(UUID tagId, boolean force)
  // ==========================================

  @Test
  @DisplayName("Should successfully delete a tag when it has no associated cards")
  void deleteTag_success_noCards() {
    // Arrange
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));
    tag.setCards(Collections.emptySet()); // Ensure no cards are associated

    // Act
    assertDoesNotThrow(() -> tagService.deleteTag(tagId, false));

    // Assert
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository).delete(tag);
  }

  @Test
  @DisplayName("Should throw NotFoundException when deleting a non-existent tag")
  void deleteTag_notFound_throwsNotFoundException() {
    // Arrange
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException thrown = assertThrows(NotFoundException.class,
        () -> tagService.deleteTag(tagId, false));

    assertEquals("Tag you are trying to delete does not exist", thrown.getMessage());
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository, never()).delete(any(Tag.class));
  }

  @Test
  @DisplayName("Should throw ConflictWithDataException when deleting a tag used by cards without force flag")
  void deleteTag_withCards_noForce_throwsConflictWithDataException() {
    // Arrange
    Set<Card> cards = new HashSet<>();
    cards.add(new Card());
    tag.setCards(cards);
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));

    // Act & Assert
    ConflictWithDataException thrown = assertThrows(ConflictWithDataException.class,
        () -> tagService.deleteTag(tagId, false));

    assertEquals("There are still cards with this tag! Are you sure you want to delete it?",
        thrown.getMessage());
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository, never()).delete(any(Tag.class));
  }

  @Test
  @DisplayName("Should successfully delete a tag and detach it from cards when force flag is true")
  void deleteTag_withCards_forceTrue_success() {
    // Arrange
    Card card1 = new Card();
    Card card2 = new Card();
    Set<Tag> card1Tags = new HashSet<>(Collections.singletonList(tag));
    Set<Tag> card2Tags = new HashSet<>(Collections.singletonList(tag));
    card1.setTags(card1Tags);
    card2.setTags(card2Tags);

    Set<Card> cards = new HashSet<>();
    cards.add(card1);
    cards.add(card2);
    tag.setCards(cards);

    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));

    // Act
    assertDoesNotThrow(() -> tagService.deleteTag(tagId, true));

    // Assert
    assertTrue(card1.getTags().isEmpty());
    assertTrue(card2.getTags().isEmpty());
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository).delete(tag);
  }

  // ==========================================
  // Tests for renameTag(UUID tagId, String newName)
  // ==========================================

  @Test
  @DisplayName("Should successfully rename a tag")
  void renameTag_success_nameChanged() {
    // Arrange
    String newName = "New Tag Name";
    Tag updatedTag = new Tag();
    updatedTag.setTagId(tagId);
    updatedTag.setName(newName);
    updatedTag.setColor("#FFFFFF");
    updatedTag.setUser(user);

    TagResponseDTO updatedTagRequestDTO = new TagResponseDTO(tagId, newName, "#FFFFFF", OffsetDateTime.now().minusDays(2), OffsetDateTime.now());

    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));
    when(tagRepository.findByName(newName)).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenReturn(updatedTag);
    when(tagMapper.toDTO(updatedTag)).thenReturn(updatedTagRequestDTO);

    // Act
    TagResponseDTO result = tagService.renameTag(tagId, newName);

    // Assert
    assertNotNull(result);
    assertEquals(newName, result.name());
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository).findByName(newName);
    verify(tagRepository).save(tag); // Ensure the original tag object is saved after modification
    verify(tagMapper).toDTO(updatedTag);
    assertEquals(newName, tag.getName()); // Verify the tag object itself was updated
  }

  @Test
  @DisplayName("Should not check for duplicates or save if renamed with the same name (case-insensitive)")
  void renameTag_success_nameSameCaseInsensitive() {
    // Arrange
    String newName = "test tag"; // Same name, different case
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));
    when(tagMapper.toDTO(tag)).thenReturn(tagResponseDTO); // Should return original DTO

    // Act
    TagResponseDTO result = tagService.renameTag(tagId, newName);

    // Assert
    assertNotNull(result);
    assertEquals(tagResponseDTO.name(), result.name());
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository, never()).findByName(anyString()); // Should not check for duplicates
    verify(tagRepository, never()).save(any(Tag.class)); // Should not save
    verify(tagMapper).toDTO(tag);
    assertEquals("Test Tag", tag.getName()); // Name should remain unchanged
  }

  @Test
  @DisplayName("Should throw NotFoundException when renaming a non-existent tag")
  void renameTag_notFound_throwsNotFoundException() {
    // Arrange
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException thrown = assertThrows(NotFoundException.class,
        () -> tagService.renameTag(tagId, "New Name"));

    assertEquals("Tag you are trying to rename does not exist", thrown.getMessage());
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository, never()).findByName(anyString());
    verify(tagRepository, never()).save(any(Tag.class));
  }

  @Test
  @DisplayName("Should throw DuplicateException when renaming to an already existing tag name")
  void renameTag_duplicateNewName_throwsDuplicateException() {
    // Arrange
    String newName = "Existing Tag";
    Tag existingTag = new Tag();
    existingTag.setTagId(UUID.randomUUID());
    existingTag.setName(newName);

    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));
    when(tagRepository.findByName(newName)).thenReturn(Optional.of(existingTag));

    // Act & Assert
    DuplicateException thrown = assertThrows(DuplicateException.class,
        () -> tagService.renameTag(tagId, newName));

    assertEquals("The tag with that name already exists", thrown.getMessage());
    verify(tagRepository).findByTagId(tagId);
    verify(tagRepository).findByName(newName);
    verify(tagRepository, never()).save(any(Tag.class));
  }

  // ==========================================
  // Tests for findByTagEntityId(UUID tagId)
  // ==========================================


  @Test
  @DisplayName("Should return Tag entity when it exists")
  void findTagEntityById_success() {
    // Arrange
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));

    // Act
    Tag result = tagService.findTagEntityById(tagId);

    // Assert
    assertNotNull(result);
    assertEquals(tag, result);
  }

  @Test
  @DisplayName("Should throw NotFoundException when Tag entity does not exist")
  void findTagEntityById_notFound_throwsNotFoundException() {
    // Arrange
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> tagService.findTagEntityById(tagId));
  }

  // ==========================================
  // Tests for findByTagId(UUID tagId)
  // ==========================================

  @Test
  @DisplayName("Should return TagDTO when Tag exists")
  void findByTagId_success() {
    // Arrange
    TagResponseDTO expectedDto = new TagResponseDTO(tagId, "testName", "testColor", OffsetDateTime.now().minusDays(3), OffsetDateTime.now());

    // Stub both the repository call (used internally) and the mapper
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.of(tag));
    when(tagMapper.toDTO(tag)).thenReturn(expectedDto);

    // Act
    TagResponseDTO result = tagService.findByTagId(tagId);

    // Assert
    assertNotNull(result);
    assertEquals(expectedDto, result);
  }

  @Test
  @DisplayName("Should propagate NotFoundException when mapped method fails")
  void findByTagId_notFound_throwsNotFoundException() {
    // Arrange
    when(tagRepository.findByTagId(tagId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> tagService.findByTagId(tagId));

    // Assert mapper was never touched because exception happened first
    verifyNoInteractions(tagMapper);
  }
}
