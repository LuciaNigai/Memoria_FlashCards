package com.lucia.memoria.service.helper;

import com.lucia.memoria.dto.local.FieldRequestDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.DuplicateException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Field;
import com.lucia.memoria.repository.CardRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardValidator {

  private final CardRepository cardRepository;

  public void validateDuplicates(
      FieldRequestDTO minimalDTO, boolean saveDuplicate, UUID currentCardId, FieldRole fieldRole) {
    String sanitizedContent = minimalDTO.getContent() != null ? minimalDTO.getContent().trim() : "";

    List<UUID> duplicateIds = cardRepository.findCardIdsByFieldRoleAndContent(fieldRole, sanitizedContent)
        .stream()
        .filter(id -> !id.equals(currentCardId))
        .toList();
    if (!duplicateIds.isEmpty() && !saveDuplicate) {
      throw new DuplicateException(
          "The card with such field " + minimalDTO.getContent()
              + " already exists. Are you sure you want to save it?", duplicateIds);
    }
  }

  public void validateCardStructure(Card card) {
    boolean hasFilledFront = false;
    boolean hasFilledBack = false;

    for (Field field : card.getFields()) {
      if (field.getTemplateField() == null || field.getContent() == null) {
        continue;
      }

      if (!field.getContent().trim().isEmpty()) {
        FieldRole role = field.getTemplateField().getFieldRole();
        if (FieldRole.FRONT == role) {
          hasFilledFront = true;
        } else if (FieldRole.BACK == role) {
          hasFilledBack = true;
        }
      }

      if (hasFilledFront && hasFilledBack) {
        return;
      }
    }

    if (!hasFilledFront || !hasFilledBack) {
      throw new ConflictWithDataException("Card must have at least one FRONT and one BACK field populated with text content");
    }
  }
}
