package com.lucia.memoria.service.local;

import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.FieldDTO;
import com.lucia.memoria.dto.local.ResponseDeckWithCardsDTO;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.mapper.CardMapper;
import com.lucia.memoria.mapper.DeckWithCardsMapper;
import com.lucia.memoria.mapper.FieldMapper;
import com.lucia.memoria.mapper.TemplateFieldMapper;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.Field;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.repository.CardRepository;
import com.lucia.memoria.service.helper.CardValidator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardService {

  private final CardRepository cardRepository;
  private final DeckService deckService;
  private final TemplateService templateService;
  private final CardMapper cardMapper;
  private final FieldMapper fieldMapper;
  private final TemplateFieldMapper templateFieldMapper;
  private final DeckWithCardsMapper deckWithCardsMapper;
  private final CardValidator cardValidator;


  @Transactional
  public CardRequestDTO createCard(CardRequestDTO cardDTO, boolean saveDuplicate) {
    // 1. Fetch relevant data
    Deck deck = deckService.getDeckEntityById(cardDTO.getDeckId());
    Template template = templateService.getTemplateEntityById(cardDTO.getTemplateId());

    // 2. Create the new card object
    Card card = new Card(deck, template);

    // 3. Process fields
    Map<UUID, TemplateField> templateFields = template.getFields().stream()
        .collect(Collectors.toMap(TemplateField::getTemplateFieldId, Function.identity()));

    Optional.ofNullable(cardDTO.getFieldRequestDTOList())
        .orElse(Collections.emptyList())
        .forEach(dto -> {
          cardValidator.validateDuplicates(dto, saveDuplicate, null);
          TemplateField tf = templateFields.get(dto.getTemplateFieldId());
          if (tf == null) {
            throw new NotFoundException("Template field not found for ID: " + dto.getTemplateFieldId());
          }

          Field newField = Field.createNew(card, tf, dto.getContent());
          card.addField(newField);
        });

    // 4. Validation & Save
    cardValidator.validateCardStructure(card);
    return cardMapper.toMinimalDTO(cardRepository.save(card));
  }

  @Transactional
  public CardRequestDTO updateCard(UUID cardId, CardRequestDTO cardDTO, boolean saveDuplicates) {
    // 1. Fetch
    Card card = cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)
        .orElseThrow(() -> new NotFoundException("Card not found"));

    // 2. Prepare context (Getting the valid template fields)
    Map<UUID, TemplateField> templateFields = card.getTemplate().getFields().stream()
        .collect(Collectors.toMap(TemplateField::getTemplateFieldId, Function.identity()));

    // 3. Cross-cutting concern (Duplicate check)
    cardDTO.getFieldRequestDTOList().forEach(dto ->
        cardValidator.validateDuplicates(dto, saveDuplicates, card.getCardId()));

    // 4. Delegation (The "tell, don't ask" principle)
    card.syncFields(cardDTO.getFieldRequestDTOList(), templateFields);

    // 5. Business Rules & save
    cardValidator.validateCardStructure(card);
    return cardMapper.toMinimalDTO(cardRepository.save(card));
  }


  @Transactional(readOnly = true)
  public CardResponseDTO getCardById(UUID cardId) {
    Card card = cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)
        .orElseThrow(() -> new NotFoundException("Invalid card ID provided."));

    // 1. Map basic info (cardId, deckId, templateId)
    CardResponseDTO cardResponseDTO = cardMapper.toDTO(card);

    // 2. Build the "Full" list (Existing fields + Blank fields from template)
    List<TemplateField> allPossibleFields = card.getTemplate().getFields();
    List<FieldDTO> fullFields = buildFullFields(card, allPossibleFields);

    // 3. Manually set it
    cardResponseDTO.setFieldDTOList(fullFields);

    return cardResponseDTO;
  }

  @Transactional(readOnly = true)
  public ResponseDeckWithCardsDTO getDeckWithCards(UUID deckId) {
    Deck deck = deckService.getDeckEntityById(deckId);
    List<Card> cards = cardRepository.findAllByDeck(deck);

    return deckWithCardsMapper.toDTO(deck, cards);
  }

  @Transactional
  public void deleteCard(UUID cardId) {
    Card card = cardRepository.findByCardId(cardId)
        .orElseThrow(() -> new NotFoundException("The card not found"));
    cardRepository.delete(card);
  }

  private List<FieldDTO> buildFullFields(Card card, List<TemplateField> templateFields) {
    return templateFields.stream()
        .map(templateField -> {
          Field field = card.getFieldByTemplateId(templateField.getTemplateFieldId()).orElse(null);
          return convertOrCreateBlankFieldDTO(templateField, field);
        }).toList();
  }

  private FieldDTO convertOrCreateBlankFieldDTO(TemplateField templateField,
      Field field) {
    return Optional.ofNullable(field)
        .map(f -> {
          FieldDTO fieldDTO = fieldMapper.toDTO(f);
          fieldDTO.setFieldTemplate(templateFieldMapper.toDTO(templateField));
          return fieldDTO;
        })
        .orElse(FieldDTO.blankWithTemplate(templateFieldMapper.toDTO(templateField)));
  }
}
