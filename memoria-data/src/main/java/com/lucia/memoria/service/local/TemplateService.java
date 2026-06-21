package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.TemplateFieldRequestDTO;
import com.lucia.memoria.dto.local.TemplateRequestDTO;
import com.lucia.memoria.dto.local.TemplateResponseDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.helper.FieldType;
import com.lucia.memoria.helper.TemplateFieldType;
import com.lucia.memoria.mapper.CardMapper;
import com.lucia.memoria.mapper.TemplateFieldMapper;
import com.lucia.memoria.mapper.TemplateMapper;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.TemplateField;
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
  public TemplateResponseDTO createTemplate(TemplateRequestDTO templateRequestDTO) {
    User owner = userService.getUserEntityById(templateRequestDTO.getOwnerId());

    Optional<Template> templateExists = templateRepository.findByNameAndOwner(templateRequestDTO.getName(),
        owner);

    if (templateExists.isPresent()) {
      throw new IllegalArgumentException(
          "Template with name " + templateRequestDTO.getName() + " already exists");
    }

    Template template = new Template();
    template.setTemplateId(UUID.randomUUID());
    template.setName(templateRequestDTO.getName());
    template.setOwner(owner);

    for (TemplateFieldRequestDTO templateFieldResponseDTO : templateRequestDTO.getFields()) {
      addTemplateField(templateFieldResponseDTO, template);
    }

    boolean hasPOS = template.getFields().stream()
        .anyMatch(f -> PART_OF_SPEECH.equalsIgnoreCase(f.getName()));
    if (templateRequestDTO.isIncludesPartOfSpeech() && !hasPOS) {
      addPartOfSpeechFieldIfNeeded(templateRequestDTO, template);
    }

    return templateMapper.toDTO(templateRepository.save(template));
  }

  @Transactional(readOnly = true)
  public TemplateResponseDTO getTemplateById(UUID templateId) {
    return templateMapper.toDTO(getTemplateEntityById(templateId));
  }

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
  public List<TemplateResponseDTO> getTemplatesByUserId(UUID userId) {
    User owner = userService.getUserEntityById(userId);

    return templateMapper.toDTOList(templateRepository.findAllByOwner(owner));
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteTemplate(UUID templateId) {
    boolean isTemplateUsed = isTemplateInUse(templateId);
    if (isTemplateUsed) {
      throw new ConflictWithDataException(
          "Template cannot be deleted. There are still cards that use that template.");
    }

    Template template = templateRepository.findTemplateByTemplateIdWithFields(templateId)
        .orElseThrow(
            () -> new NotFoundException("Template you are trying to delete does not exist"));

    templateRepository.delete(template);
  }

  private boolean isTemplateInUse(UUID templateId) {
    return cardRepository.countByTemplateTemplateId(templateId) > 0;
  }


  private void addTemplateField(TemplateFieldRequestDTO templateFieldRequestDTO, Template template) {
    TemplateFieldType templateFieldType =
        templateFieldRequestDTO.getTemplateFieldType() == null ? new TemplateFieldType(FieldType.TEXT)
            : templateFieldRequestDTO.getTemplateFieldType();
    TemplateField templateField = templateFieldMapper.toEntity(templateFieldRequestDTO);
    templateField.setTemplateFieldId(UUID.randomUUID());
    templateField.setTemplateFieldType(templateFieldType);
    template.addField(templateField);
  }

  private static void addPartOfSpeechFieldIfNeeded(TemplateRequestDTO templateRequestDTO, Template template) {
    if (templateRequestDTO.isIncludesPartOfSpeech()) {
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
