package com.lucia.memoria.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.repository.CardRepository;
import com.lucia.memoria.repository.DeckRepository;
import com.lucia.memoria.repository.TemplateFieldRepository;
import com.lucia.memoria.repository.TemplateRepository;
import com.lucia.memoria.service.local.CardService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class CardServiceIntegrationTest {

  @Autowired
  private CardService cardService;

  @Autowired
  private DeckRepository deckRepository;

  @Autowired
  private TemplateRepository templateRepository;

  @Autowired
  private TemplateFieldRepository templateFieldRepository;

  @Autowired
  private CardRepository cardRepository;

  private Deck deck;
  private Template template;
  private TemplateField frontField;
  private TemplateField backField;

  @Test
  public void testGetCardByIdWhenCardIdIsNullShouldThrowException() {
    // act
    Exception exception = assertThrows(NullPointerException.class, () -> cardService.getCardById(null));
    //assert
    assertEquals("cardId must not be null", exception.getMessage());
  }

  @Test
  public void testGetCardByIdWhenCardIsNotFoundShouldThrowException() {
    // act
    Exception exception = assertThrows(NotFoundException.class,
        () -> cardService.getCardById(UUID.randomUUID()));
    // assert
    assertEquals("Invalid card ID provided.", exception.getMessage());
  }
}
