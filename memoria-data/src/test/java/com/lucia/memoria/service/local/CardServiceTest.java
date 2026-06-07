package com.lucia.memoria.service.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.FieldDTO;
import com.lucia.memoria.dto.local.FieldMinimalDTO;
import com.lucia.memoria.dto.local.ResponseDeckWithCardsDTO;
import com.lucia.memoria.dto.local.TemplateFieldDTO;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.helper.FieldType;
import com.lucia.memoria.helper.TemplateFieldType;
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
@DisplayName("CardService Unit Tests")
class CardServiceTest {

  @Mock
  private CardRepository cardRepository;
  @Mock
  private DeckService deckService;
  @Mock
  private TemplateService templateService;
  @Mock
  private CardMapper cardMapper;
  @Mock
  private FieldMapper fieldMapper;
  @Mock
  private TemplateFieldMapper templateFieldMapper;
  @Mock
  private DeckWithCardsMapper deckWithCardsMapper;
  @Mock
  private CardValidator cardValidator;

  @InjectMocks
  private CardService cardService;

  private UUID deckId;
  private UUID templateId;
  private UUID cardId;
  private Deck deck;
  private Template template;
  private Card card;
  private CardRequestDTO cardRequestDTO;
  private CardResponseDTO cardResponseDTO;

  @BeforeEach
  void setUp() {
    deckId = UUID.randomUUID();
    templateId = UUID.randomUUID();
    cardId = UUID.randomUUID();

    deck = new Deck();
    template = new Template();
    template.setFields(new ArrayList<>());
    
    card = new Card(deck, template);
    card.setCardId(cardId);

    cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(new ArrayList<>());

    cardResponseDTO = new CardResponseDTO();
    cardResponseDTO.setId(cardId);
  }

  @Test
  @DisplayName("Should successfully create a card with fields")
  void createCard_success() {
    // Arrange
    UUID templateFieldId = UUID.randomUUID();
    TemplateField tf = new TemplateField();
    tf.setTemplateFieldId(templateFieldId);
    tf.setTemplateFieldType(new TemplateFieldType(FieldType.TEXT));
    template.getFields().add(tf);

    FieldMinimalDTO fieldDto = new FieldMinimalDTO();
    fieldDto.setTemplateFieldId(templateFieldId);
    fieldDto.setContent("Test Content");
    cardRequestDTO.getFields().add(fieldDto);

    when(deckService.getDeckEntityById(deckId)).thenReturn(deck);
    when(templateService.getTemplateEntityById(templateId)).thenReturn(template);
    when(cardRepository.save(any(Card.class))).thenReturn(card);
    when(cardMapper.toResponseDTO(card)).thenReturn(cardResponseDTO);

    // Act
    CardResponseDTO result = cardService.createCard(cardRequestDTO, false);

    // Assert
    assertNotNull(result);
    verify(cardValidator).validateDuplicates(any(), anyBoolean(), any());
    verify(cardValidator).validateCardStructure(any());
    verify(cardRepository).save(any(Card.class));
  }

  @Test
  @DisplayName("Should successfully create a card when no fields are provided")
  void createCard_noFields_success() {
    // Arrange
    cardRequestDTO.setFields(null);

    when(deckService.getDeckEntityById(deckId)).thenReturn(deck);
    when(templateService.getTemplateEntityById(templateId)).thenReturn(template);
    when(cardRepository.save(any(Card.class))).thenReturn(card);
    when(cardMapper.toResponseDTO(card)).thenReturn(cardResponseDTO);

    // Act
    CardResponseDTO result = cardService.createCard(cardRequestDTO, false);

    // Assert
    assertNotNull(result);
    verify(cardRepository).save(any(Card.class));
  }

  @Test
  @DisplayName("Should throw NotFoundException when a template field ID in request is unknown")
  void createCard_templateFieldNotFound_throwsNotFoundException() {
    // Arrange
    UUID unknownTemplateFieldId = UUID.randomUUID();
    FieldMinimalDTO fieldDto = new FieldMinimalDTO();
    fieldDto.setTemplateFieldId(unknownTemplateFieldId);
    cardRequestDTO.getFields().add(fieldDto);

    when(deckService.getDeckEntityById(deckId)).thenReturn(deck);
    when(templateService.getTemplateEntityById(templateId)).thenReturn(template);

    // Act & Assert
    assertThrows(NotFoundException.class, () -> cardService.createCard(cardRequestDTO, false));
  }

  @Test
  @DisplayName("Should successfully update a card")
  void updateCard_success() {
    // Arrange
    when(cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)).thenReturn(Optional.of(card));
    when(cardRepository.save(any(Card.class))).thenReturn(card);
    when(cardMapper.toResponseDTO(card)).thenReturn(cardResponseDTO);

    // Act
    CardResponseDTO result = cardService.updateCard(cardId, cardRequestDTO, false);

    // Assert
    assertNotNull(result);
    verify(cardRepository).save(card);
  }

  @Test
  @DisplayName("Should successfully update a card when no fields are provided")
  void updateCard_noFields_success() {
    // Arrange
    cardRequestDTO.setFields(new ArrayList<>());
    when(cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)).thenReturn(Optional.of(card));
    when(cardRepository.save(any(Card.class))).thenReturn(card);
    when(cardMapper.toResponseDTO(card)).thenReturn(cardResponseDTO);

    // Act
    CardResponseDTO result = cardService.updateCard(cardId, cardRequestDTO, false);

    // Assert
    assertNotNull(result);
    verify(cardRepository).save(card);
  }

  @Test
  @DisplayName("Should successfully get a card by ID with all possible template fields")
  void getCardById_success() {
    // Arrange
    UUID templateFieldId = UUID.randomUUID();
    TemplateField tf = new TemplateField();
    tf.setTemplateFieldId(templateFieldId);
    template.getFields().add(tf);

    when(cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)).thenReturn(Optional.of(card));
    when(cardMapper.toResponseDTO(card)).thenReturn(cardResponseDTO);
    when(templateFieldMapper.toDTO(tf)).thenReturn(new TemplateFieldDTO());

    // Act
    CardResponseDTO result = cardService.getCardById(cardId);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getFields());
    assertEquals(1, result.getFields().size());
    verify(cardRepository).findByCardIdWithFieldsAndFieldTemplates(cardId);
  }

  @Test
  @DisplayName("Should successfully get a card by ID including existing field data")
  void getCardById_withExistingField_success() {
    // Arrange
    UUID templateFieldId = UUID.randomUUID();
    TemplateField tf = new TemplateField();
    tf.setTemplateFieldId(templateFieldId);
    template.getFields().add(tf);

    Field field = new Field();
    field.setTemplateField(tf);
    card.getFields().add(field);

    when(cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)).thenReturn(Optional.of(card));
    when(cardMapper.toResponseDTO(card)).thenReturn(cardResponseDTO);
    
    FieldDTO fieldDto = new FieldDTO();
    when(fieldMapper.toDTO(field)).thenReturn(fieldDto);
    when(templateFieldMapper.toDTO(tf)).thenReturn(new TemplateFieldDTO());

    // Act
    CardResponseDTO result = cardService.getCardById(cardId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getFields().size());
  }

  @Test
  @DisplayName("Should throw NotFoundException when card ID is not found")
  void getCardById_notFound_throwsNotFoundException() {
    // Arrange
    when(cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> cardService.getCardById(cardId));
  }

  @Test
  @DisplayName("Should successfully retrieve a deck with all its cards")
  void getDeckWithCards_success() {
    // Arrange
    List<Card> cards = Collections.singletonList(card);
    ResponseDeckWithCardsDTO expectedResponse = new ResponseDeckWithCardsDTO();

    when(deckService.getDeckEntityById(deckId)).thenReturn(deck);
    when(cardRepository.findAllByDeck(deck)).thenReturn(cards);
    when(deckWithCardsMapper.toDTO(deck, cards)).thenReturn(expectedResponse);

    // Act
    ResponseDeckWithCardsDTO result = cardService.getDeckWithCards(deckId);

    // Assert
    assertNotNull(result);
    verify(deckService).getDeckEntityById(deckId);
    verify(cardRepository).findAllByDeck(deck);
  }

  @Test
  @DisplayName("Should successfully delete a card")
  void deleteCard_success() {
    // Arrange
    when(cardRepository.findByCardId(cardId)).thenReturn(Optional.of(card));

    // Act
    cardService.deleteCard(cardId);

    // Assert
    verify(cardRepository).delete(card);
  }

  @Test
  @DisplayName("Should throw NotFoundException when deleting a non-existent card")
  void deleteCard_notFound_throwsException() {
    // Arrange
    when(cardRepository.findByCardId(cardId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> cardService.deleteCard(cardId));
  }
}
