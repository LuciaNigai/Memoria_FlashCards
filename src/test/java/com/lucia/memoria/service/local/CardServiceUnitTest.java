package com.lucia.memoria.service.local;

import static com.lucia.memoria.service.local.TestUtils.createField;
import static com.lucia.memoria.service.local.TestUtils.createFieldDTO;
import static com.lucia.memoria.service.local.TestUtils.createTemplateField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lucia.memoria.dto.local.CardDTO;
import com.lucia.memoria.dto.local.CardMinimalDTO;
import com.lucia.memoria.dto.local.FieldDTO;
import com.lucia.memoria.dto.local.FieldMinimalDTO;
import com.lucia.memoria.dto.local.TemplateFieldDTO;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CardServiceUnitTest {

  @Mock
  private CardRepository cardRepository;

  @Mock
  private DeckService deckService;

  @Mock
  private TemplateService templateService;

  @Mock
  private TemplateFieldService templateFieldService;

  @Mock
  private CardMapper cardMapper;

  @Mock
  private FieldMapper fieldMapper;

  @Mock
  private TemplateFieldMapper templateFieldMapper;

  @Mock
  private DeckWithCardsMapper deckWithCardsMapper;

  @InjectMocks
  private CardService cardService;

  @Test
  public void testGetCardByIdWhenCardIdIsNullShouldThrowException() {
    //act
    Exception exception = assertThrows(NullPointerException.class,
        () -> cardService.getCardById(null));
    //assert
    assertEquals("cardId must not be null", exception.getMessage());
  }

  @Test
  public void testGetCardByIdWhenCardIsNotFoundShouldThrowException() {
    // arrange
    UUID cardId = UUID.randomUUID();
    when(cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId))
        .thenReturn(Optional.empty());
    // act
    Exception exception = assertThrows(NotFoundException.class,
        () -> cardService.getCardById(cardId));
    // assert
    assertEquals("Invalid card ID provided.", exception.getMessage());
  }

  @Test
  public void testGetCardByIdReturnsCardSuccessfully() {
    // arrange
    UUID cardId = UUID.randomUUID();
    UUID frontFieldId = UUID.randomUUID();
    UUID backFieldId = UUID.randomUUID();

    TemplateField frontTemplate = createTemplateField(frontFieldId);
    TemplateField backTemplate = createTemplateField(backFieldId);

    Field front = createField(frontTemplate);
    Field back = createField(backTemplate);

    Card card = new Card();
    card.setFields(List.of(front, back));

    TemplateFieldDTO frontTemplateDTO = new TemplateFieldDTO();
    TemplateFieldDTO backTemplateDTO = new TemplateFieldDTO();

    FieldDTO frontFieldDTO = createFieldDTO(frontTemplateDTO);
    FieldDTO backFieldDTO = createFieldDTO(backTemplateDTO);

    CardDTO cardDTO = new CardDTO();
    cardDTO.setFieldDTOList(List.of(frontFieldDTO, backFieldDTO));

    // stubbing
    when(cardRepository.findByCardIdWithFieldsAndFieldTemplates(cardId)).thenReturn(
        Optional.of(card));
    when(cardMapper.toDTO(card)).thenReturn(cardDTO);
    when(fieldMapper.toDTO(front)).thenReturn(frontFieldDTO);
    when(fieldMapper.toDTO(back)).thenReturn(backFieldDTO);
    when(templateFieldMapper.toDTO(frontTemplate)).thenReturn(frontTemplateDTO);
    when(templateFieldMapper.toDTO(backTemplate)).thenReturn(backTemplateDTO);

    // act
    CardDTO result = cardService.getCardById(cardId);

    // assert
    assertNotNull(result);
    assertEquals(2, result.getFieldDTOList().size());
    assertSame(frontFieldDTO, result.getFieldDTOList().get(0));
    assertSame(backFieldDTO, result.getFieldDTOList().get(1));
    assertSame(frontTemplateDTO, result.getFieldDTOList().get(0).getFieldTemplate());
    assertSame(backTemplateDTO, result.getFieldDTOList().get(1).getFieldTemplate());
  }

  @Test
  public void testDeleteCardWhenCardNotFound() {
    // arrange
    when(cardRepository.findByCardId(any())).thenReturn(Optional.empty());
    // act
    Exception exception = assertThrows(NotFoundException.class,
        () -> cardService.deleteCard(UUID.randomUUID()));
    // assert
    assertEquals("The card not found", exception.getMessage());
  }

  @Test
  public void testDeleteCardSuccessfully() {
    Card card = new Card();
    // arrange
    when(cardRepository.findByCardId(any())).thenReturn(Optional.of(card));
    // act
    cardService.deleteCard(UUID.randomUUID());
    // assert
    verify(cardRepository).delete(card);
  }

  @Test
  public void testCreateCardWhenDeckNotFound() {
    // stub
    when(deckService.getDeckEntityById(any())).thenThrow(new NotFoundException("Deck not found."));
    // act
    Exception exception = assertThrows(NotFoundException.class,
        () -> cardService.createCard(new CardMinimalDTO(), false));
    // assert
    assertEquals("Deck not found.", exception.getMessage());
  }

  @Test
  public void testCreateCardWhenTemplateNotFound() {
    // arrange
    Deck deck = new Deck();
    //stub
    when(deckService.getDeckEntityById(any())).thenReturn(deck);
    when(templateService.getTemplateEntityById(any())).thenThrow(new NotFoundException("Template Not found"));
    //act
    Exception exception = assertThrows(NotFoundException.class, () -> cardService.createCard(new CardMinimalDTO(), false));
    // assert
    assertEquals("Template Not found", exception.getMessage());
  }

  @Test
  public void testCreateCardWhenTemplateFieldIsNotFound() {
    // arrange
    Deck deck = new Deck();
    Template template = new Template();
    TemplateField templateField = new TemplateField();
    template.addField(templateField);
    CardMinimalDTO cardMinimalDTO = new CardMinimalDTO();
    cardMinimalDTO.setFieldMinimalDTOList(new ArrayList<>(Arrays.asList(new FieldMinimalDTO(), new FieldMinimalDTO())));
    //stub
    when(deckService.getDeckEntityById(any())).thenReturn(deck);
    when(templateService.getTemplateEntityById(any())).thenReturn(template);
    when(templateFieldService.findTemplateFieldById(any())).thenThrow(new IllegalArgumentException("Target template field does not exists"));

    Exception exception = assertThrows(IllegalArgumentException.class, () -> cardService.createCard(cardMinimalDTO, false));
    assertEquals("Target template field does not exists", exception.getMessage());
  }

  @Test
  public void testCreateCardWhenTemplateFieldTypeIsNull() {
    // arrange
    Deck deck = new Deck();
    Template template = new Template();
    TemplateField templateFieldFront = new TemplateField();
    templateFieldFront.setTemplateFieldId(UUID.randomUUID());
    template.addField(templateFieldFront);
    template.addField(templateFieldFront);
    CardMinimalDTO cardMinimalDTO = new CardMinimalDTO();
    cardMinimalDTO.setFieldMinimalDTOList(new ArrayList<>(Arrays.asList(new FieldMinimalDTO(), new FieldMinimalDTO())));
    //stub
    when(deckService.getDeckEntityById(any())).thenReturn(deck);
    when(templateService.getTemplateEntityById(any())).thenReturn(template);
    when(templateFieldService.findTemplateFieldById(any())).thenReturn(templateFieldFront);
    // act
    Exception exception = assertThrows(IllegalArgumentException.class, () -> cardService.createCard(cardMinimalDTO, false));
    // assert
    assertEquals("TemplateFieldType must not be null", exception.getMessage());
  }



}