package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.TemplateFieldDTO;
import com.lucia.memoria.dto.local.TemplateDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.helper.FieldType;
import com.lucia.memoria.helper.TemplateFieldType;
import com.lucia.memoria.mapper.CardMapper;
import com.lucia.memoria.mapper.TemplateFieldMapper;
import com.lucia.memoria.mapper.TemplateMapper;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.CardRepository;
import com.lucia.memoria.repository.TemplateRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemplateService {

  private static final List<String> PARTS_OF_SPEECH = List.of(
      "noun", "pronoun", "verb", "adjective", "adverb", "preposition", "conjunction", "interjection"
  );
  private static final String PART_OF_SPEECH = "Part of Speech";

  private final TemplateRepository templateRepository;
  private final UserService userService;
  private final CardRepository cardRepository;
  private final TemplateFieldMapper templateFieldMapper;
  private final TemplateMapper templateMapper;
  private final CardMapper cardMapper;

  @Transactional(propagation = Propagation.REQUIRED)
  public TemplateDTO createTemplate(TemplateDTO templateDTO) {
    User owner = userService.getUserEntityById(templateDTO.getOwnerId());

    Optional<Template> templateExists = templateRepository.findByNameAndOwner(templateDTO.getName(),
        owner);

    if (templateExists.isPresent()) {
      throw new IllegalArgumentException(
          "Template with name " + templateDTO.getName() + " already exists");
    }

    Template template = new Template();
    template.setTemplateId(UUID.randomUUID());
    template.setName(templateDTO.getName());
    template.setOwner(owner);

    for (TemplateFieldDTO templateFieldDTO : templateDTO.getFields()) {
      addTemplateField(templateFieldDTO, template);
    }

    boolean hasPOS = template.getFields().stream()
        .anyMatch(f -> PART_OF_SPEECH.equalsIgnoreCase(f.getName()));
    if (templateDTO.getIncludesPartOfSpeech() && !hasPOS) {
      addPartOfSpeechFieldIfNeeded(templateDTO, template);
    }

    return templateMapper.toDTO(templateRepository.save(template));
  }

  @Transactional(readOnly = true)
  public TemplateDTO getTemplateById(UUID templateId) {
    return templateMapper.toDTO(getTemplateEntityById(templateId));
  }

  @Transactional(readOnly = true)
  public Template getTemplateEntityById(UUID templateId) {
    return templateRepository.findByTemplateId(templateId)
        .orElseThrow(() -> new NotFoundException("Template Not found"));
  }

  @Transactional(readOnly = true)
  public Template getTemplateByName(String name) {
    return templateRepository.findTemplateByTemplateNameWithFields(name)
        .orElseThrow(() -> new NotFoundException("Template not found exception"));
  }

  @Transactional(readOnly = true)
  public List<TemplateDTO> getTemplatesByUserId(UUID userId) {
    User owner = userService.getUserEntityById(userId);

    return templateMapper.toDTOList(templateRepository.findAllByOwner(owner));
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteTemplate(UUID templateId) {
    Template template = templateRepository.findTemplateByTemplateIdWithFields(templateId)
        .orElseThrow(
            () -> new NotFoundException("Template you are trying to delete does not exist"));

    List<Card> templateCards = cardRepository.findByTemplate(template);
    if (!templateCards.isEmpty()) {
      throw new ConflictWithDataException(
          "Template cannot be deleted. There are still cards that use that template.",
          cardMapper.toMinimalDTOList(templateCards));
    }
    templateRepository.delete(template);
  }


  private void addTemplateField(TemplateFieldDTO templateFieldDTO, Template template) {
    TemplateFieldType templateFieldType =
        templateFieldDTO.getTemplateFieldType() == null ? new TemplateFieldType(FieldType.TEXT)
            : templateFieldDTO.getTemplateFieldType();
    TemplateField templateField = templateFieldMapper.toEntity(templateFieldDTO);
    templateField.setTemplateFieldId(UUID.randomUUID());
    templateField.setTemplateFieldType(templateFieldType);
    template.addField(templateField);
  }

  private static void addPartOfSpeechFieldIfNeeded(TemplateDTO templateDTO, Template template) {
    if (templateDTO.getIncludesPartOfSpeech()) {
      TemplateField templateField = new TemplateField();
      templateField.setTemplateFieldId(UUID.randomUUID());
      templateField.setName(PART_OF_SPEECH);
      templateField.setFieldRole(FieldRole.AUXILIARY);
      templateField.setTemplateFieldType(new TemplateFieldType(
          FieldType.ENUM,
          PARTS_OF_SPEECH
      ));
      template.addField(templateField);
      template.setIncludesPartOfSpeech(true);
    }
  }
}
