package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.TemplateFieldRequestDTO;
import com.lucia.memoria.dto.local.TemplateRequestDTO;
import com.lucia.memoria.dto.local.TemplateResponseDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.mapper.TemplateFieldMapper;
import com.lucia.memoria.mapper.TemplateMapper;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.model.User;
import com.lucia.memoria.repository.CardRepository;
import com.lucia.memoria.repository.TemplateRepository;
import com.lucia.memoria.repository.UserRepository;
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

  private final TemplateRepository templateRepository;
  private final UserRepository userRepository;
  private final CardRepository cardRepository;
  private final TemplateFieldMapper templateFieldMapper;
  private final TemplateMapper templateMapper;

  @Transactional(propagation = Propagation.REQUIRED)
  public TemplateResponseDTO createTemplate(TemplateRequestDTO templateRequestDTO) {
    User owner = findUserOrThrow(templateRequestDTO.getOwnerId());

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
    User owner = findUserOrThrow(userId);

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

  private User findUserOrThrow(UUID userId) {
    return userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private boolean isTemplateInUse(UUID templateId) {
    return cardRepository.countByTemplateTemplateId(templateId) > 0;
  }


  private void addTemplateField(TemplateFieldRequestDTO templateFieldRequestDTO, Template template) {
    TemplateField templateField = templateFieldMapper.toEntity(templateFieldRequestDTO);
    templateField.setTemplateFieldId(UUID.randomUUID());
    template.addField(templateField);
  }
}
