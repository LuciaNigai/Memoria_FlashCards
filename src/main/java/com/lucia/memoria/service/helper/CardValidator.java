package com.lucia.memoria.service.helper;

import com.lucia.memoria.dto.local.FieldMinimalDTO;
import com.lucia.memoria.exception.DuplicateException;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Field;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.repository.CardRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardValidator {

  private final CardRepository cardRepository;

  public void validateDuplicates(
      FieldMinimalDTO minimalDTO, boolean saveDuplicate, UUID currentCardId) {
    List<UUID> duplicateIds = cardRepository.findCardIdsByFieldContent(minimalDTO.getContent())
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
    Set<FieldRole> roles = card.getFields().stream()
        .map(Field::getTemplateField)
        .map(TemplateField::getFieldRole)
        .collect(Collectors.toSet());

    if (!roles.contains(FieldRole.FRONT) || !roles.contains(FieldRole.BACK)) {
      throw new IllegalArgumentException("Card must have at least one FRONT and one BACK field");
    }
  }
}
