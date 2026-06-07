package com.lucia.memoria.service.local;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lucia.memoria.dto.local.TemplateDTO;
import com.lucia.memoria.dto.local.TemplateFieldDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.mapper.CardMapper;
import com.lucia.memoria.mapper.TemplateFieldMapper;
import com.lucia.memoria.mapper.TemplateMapper;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.CardRepository;
import com.lucia.memoria.repository.TemplateRepository;
import java.util.ArrayList;
import java.util.Collections;
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
@DisplayName("TemplateService Unit Tests")
class TemplateServiceTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private UserService userService;

  @Mock
  private CardRepository cardRepository;

  @Mock
  private TemplateFieldMapper templateFieldMapper;

  @Mock
  private TemplateMapper templateMapper;

  @Mock
  private CardMapper cardMapper;

  @InjectMocks
  private TemplateService templateService;

  private UUID userId;
  private User user;
  private UUID templateId;
  private Template template;
  private TemplateDTO templateDTO;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = new User();
    user.setUserId(userId);

    templateId = UUID.randomUUID();
    template = new Template();
    template.setTemplateId(templateId);
    template.setName("Standard Vocabulary");
    template.setOwner(user);

    templateDTO = new TemplateDTO();
    templateDTO.setName("Standard Vocabulary");
    templateDTO.setOwnerId(userId);
    templateDTO.setFields(new ArrayList<>());
  }

  @Test
  @DisplayName("Should successfully create a new template")
  void createTemplate_success() {
    // Arrange
    TemplateFieldDTO fieldDTO = new TemplateFieldDTO();
    fieldDTO.setName("Word");
    fieldDTO.setFieldRole(FieldRole.FRONT);
    templateDTO.getFields().add(fieldDTO);

    TemplateField fieldEntity = new TemplateField();
    fieldEntity.setName("Word");

    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(templateRepository.findByNameAndOwner(templateDTO.getName(), user))
        .thenReturn(Optional.empty());
    when(templateFieldMapper.toEntity(any(TemplateFieldDTO.class))).thenReturn(fieldEntity);
    when(templateRepository.save(any(Template.class))).thenReturn(template);
    when(templateMapper.toDTO(any(Template.class))).thenReturn(templateDTO);

    // Act
    TemplateDTO result = templateService.createTemplate(templateDTO);

    // Assert
    assertNotNull(result);
    assertEquals(templateDTO.getName(), result.getName());
    verify(templateRepository).save(any(Template.class));
    verify(templateFieldMapper).toEntity(any(TemplateFieldDTO.class));
  }

  @Test
  @DisplayName("Should add Part of Speech field when requested during template creation")
  void createTemplate_withPartOfSpeech_addsField() {
    // Arrange
    templateDTO.setIncludesPartOfSpeech(true);
    
    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(templateRepository.findByNameAndOwner(templateDTO.getName(), user))
        .thenReturn(Optional.empty());
    when(templateRepository.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(templateMapper.toDTO(any(Template.class))).thenReturn(templateDTO);

    // Act
    templateService.createTemplate(templateDTO);

    // Assert
    verify(templateRepository).save(argThat(t -> 
        t.getFields().stream().anyMatch(f -> "Part of Speech".equals(f.getName()))
    ));
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when creating a template with an existing name for the user")
  void createTemplate_duplicateName_throwsIllegalArgumentException() {
    // Arrange
    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(templateRepository.findByNameAndOwner(templateDTO.getName(), user))
        .thenReturn(Optional.of(template));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> templateService.createTemplate(templateDTO));
    
    verify(templateRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should successfully retrieve a template by ID")
  void getTemplateById_success() {
    // Arrange
    when(templateRepository.findByTemplateId(templateId)).thenReturn(Optional.of(template));
    when(templateMapper.toDTO(template)).thenReturn(templateDTO);

    // Act
    TemplateDTO result = templateService.getTemplateById(templateId);

    // Assert
    assertNotNull(result);
    verify(templateRepository).findByTemplateId(templateId);
  }

  @Test
  @DisplayName("Should throw NotFoundException when template ID is not found")
  void getTemplateById_notFound_throwsNotFoundException() {
    // Arrange
    when(templateRepository.findByTemplateId(templateId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> templateService.getTemplateById(templateId));
  }

  @Test
  @DisplayName("Should successfully delete a template when it is not in use")
  void deleteTemplate_success() {
    // Arrange
    when(templateRepository.findTemplateByTemplateIdWithFields(templateId))
        .thenReturn(Optional.of(template));
    when(cardRepository.findByTemplate(template)).thenReturn(Collections.emptyList());

    // Act
    templateService.deleteTemplate(templateId);

    // Assert
    verify(templateRepository).delete(template);
  }

  @Test
  @DisplayName("Should throw ConflictWithDataException when deleting a template that is in use by cards")
  void deleteTemplate_inUseByCards_throwsConflictWithDataException() {
    // Arrange
    when(templateRepository.findTemplateByTemplateIdWithFields(templateId))
        .thenReturn(Optional.of(template));
    when(cardRepository.findByTemplate(template)).thenReturn(List.of(new Card()));

    // Act & Assert
    assertThrows(ConflictWithDataException.class, () -> templateService.deleteTemplate(templateId));
    
    verify(templateRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should successfully retrieve all templates for a user")
  void getTemplatesByUserId_success() {
    // Arrange
    when(userService.getUserEntityById(userId)).thenReturn(user);
    when(templateRepository.findAllByOwner(user)).thenReturn(List.of(template));
    when(templateMapper.toDTOList(any())).thenReturn(List.of(templateDTO));

    // Act
    List<TemplateDTO> results = templateService.getTemplatesByUserId(userId);

    // Assert
    assertEquals(1, results.size());
    verify(templateRepository).findAllByOwner(user);
  }
}
