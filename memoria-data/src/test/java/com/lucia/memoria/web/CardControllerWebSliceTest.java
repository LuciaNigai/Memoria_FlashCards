package com.lucia.memoria.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucia.memoria.controller.CardController;
import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.FieldRequestDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.DuplicateException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.service.local.CardService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CardController.class)
class CardControllerWebSliceTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private CardService cardService;


  @Test
  @DisplayName("POST /api/data/cards - Create card and return 201 Created")
  void createCard_shouldReturn201() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    UUID cardId = UUID.randomUUID();
    CardResponseDTO cardResponseDTO = new CardResponseDTO();
    cardResponseDTO.setId(cardId);
    cardResponseDTO.setDeckId(deckId);

    when(cardService.createCard(any(CardRequestDTO.class), eq(false))).thenReturn(cardResponseDTO);

    // When & Then
    mockMvc.perform(post("/api/data/cards")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cardRequestDTO))
        .param("saveDuplicate", "false"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(cardId.toString()))
        .andExpect(jsonPath("$.deckId").value(deckId.toString()));
    verify(cardService).createCard(any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("POST /api/data/cards - Should return 400 Bad request when deck id is is missing")
  void createCard_deckIdIdMissing_shouldReturn400() throws Exception{
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(null);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    mockMvc.perform(post("/api/data/cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0]").value("deckId should be present"));
  }

  @Test
  @DisplayName("POST /api/data/cards - Should return 400 Bad request when card has only one field")
  void createCard_onlyOneFieldProvided_shouldReturn400() throws Exception{
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO));

    mockMvc.perform(post("/api/data/cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0]").value("Card should have at least two fields"));
  }

  @Test
  @DisplayName("POST /api/data/cards - Create card and return 404 when Field's template field not found")
  void createCard_fieldsTemplateNotFound_shouldReturn404() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    String errorMessage = "Template field not found for ID: " + templateId;

    when(cardService.createCard(any(CardRequestDTO.class), eq(false))).thenThrow(new NotFoundException(errorMessage));

    // When & Then
    mockMvc.perform(post("/api/data/cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).createCard(any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("POST /api/data/cards - Create card and return 409 when invalid card structure")
  void createCard_invalidCardFields_shouldReturn409() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    String errorMessage = "Card must have at least one FRONT and one BACK field populated with text content";

    when(cardService.createCard(any(CardRequestDTO.class), eq(false))).thenThrow(new ConflictWithDataException(errorMessage));

    // When & Then
    mockMvc.perform(post("/api/data/cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).createCard(any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("POST /api/data/cards - Create card and return 409 when duplicate card with saveDuplicate false")
  void createCard_duplicateCardWithSaveDuplicateFalse_shouldReturn409() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    String errorMessage = "The card with such field already exists. Are you sure you want to save it?";

    when(cardService.createCard(any(CardRequestDTO.class), eq(false))).thenThrow(new DuplicateException(errorMessage, List.of(UUID.randomUUID())));

    // When & Then
    mockMvc.perform(post("/api/data/cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).createCard(any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("PATCH /api/data/cards/{cardId} - update card with new data")
  void updateCard_shouldReturn200() throws Exception{
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    UUID cardId = UUID.randomUUID();
    CardResponseDTO cardResponseDTO = new CardResponseDTO();
    cardResponseDTO.setId(cardId);
    cardResponseDTO.setDeckId(deckId);

    when(cardService.updateCard(eq(cardId), any(CardRequestDTO.class), eq(false))).thenReturn(cardResponseDTO);

    // When & Then
    mockMvc.perform(patch("/api/data/cards/{cardId}", cardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(cardId.toString()))
        .andExpect(jsonPath("$.deckId").value(deckId.toString()));
    verify(cardService).updateCard(eq(cardId), any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("PATCH /api/data/cards/{cardId} - Should return 404 when card not found")
  void updateCard_cardNotFound_shouldReturn404() throws Exception{
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    UUID cardId = UUID.randomUUID();
    String errorMessage = "Card not found";

    when(cardService.updateCard(eq(cardId), any(CardRequestDTO.class), eq(false))).thenThrow(new NotFoundException(errorMessage));

    // When & Then
    mockMvc.perform(patch("/api/data/cards/{cardId}", cardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).updateCard(eq(cardId), any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("PATCH /api/data/cards/{cardId} - Should return 409 when card has invalid fields")
  void updateCard_invalidCardFields_shouldReturn409() throws Exception{
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    UUID cardId = UUID.randomUUID();
    String errorMessage = "Card must have at least one FRONT and one BACK field populated with text content";

    when(cardService.updateCard(eq(cardId), any(CardRequestDTO.class), eq(false))).thenThrow(new ConflictWithDataException(errorMessage));

    // When & Then
    mockMvc.perform(patch("/api/data/cards/{cardId}", cardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).updateCard(eq(cardId), any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("PATCH /api/data/cards/{cardId} - Should return 409 Conflict when duplicate card detected and saveDuplicate is false")
  void updateCard_duplicateCardAndSaveDuplicateFalse_shouldReturn409() throws Exception{
    // Given
    UUID deckId = UUID.randomUUID();
    UUID templateId = UUID.randomUUID();
    FieldRequestDTO fieldRequestDTO = new FieldRequestDTO();
    fieldRequestDTO.setTemplateFieldId(UUID.randomUUID());
    FieldRequestDTO secondFieldRequestDTO = new FieldRequestDTO();
    secondFieldRequestDTO.setTemplateFieldId(UUID.randomUUID());

    CardRequestDTO cardRequestDTO = new CardRequestDTO();
    cardRequestDTO.setDeckId(deckId);
    cardRequestDTO.setTemplateId(templateId);
    cardRequestDTO.setFields(List.of(fieldRequestDTO, secondFieldRequestDTO));

    UUID cardId = UUID.randomUUID();
    String errorMessage = "The card with such field already exists. Are you sure you want to save it?";

    when(cardService.updateCard(eq(cardId), any(CardRequestDTO.class), eq(false))).thenThrow(new DuplicateException(errorMessage, UUID.randomUUID()));

    // When & Then
    mockMvc.perform(patch("/api/data/cards/{cardId}", cardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardRequestDTO))
            .param("saveDuplicate", "false"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).updateCard(eq(cardId), any(CardRequestDTO.class), eq(false));
  }

  @Test
  @DisplayName("GET /api/data/cards/{cardId} - Should return card and 200 OK")
  void getCardById_shouldReturn200() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    CardResponseDTO cardResponseDTO = new CardResponseDTO();
    cardResponseDTO.setId(cardId);
    cardResponseDTO.setDeckId(deckId);

    when(cardService.getCardById(cardId)).thenReturn(cardResponseDTO);

    // When && Then
    mockMvc.perform(get("/api/data/cards/{cardId}", cardId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(cardId.toString()))
        .andExpect(jsonPath("$.deckId").value(deckId.toString()));
    verify(cardService).getCardById(cardId);
  }

  @Test
  @DisplayName("GET /api/data/cards/{cardId} - Should return 404 when card not found")
  void getCardById_cardNotFound_shouldReturn404() throws Exception {
    // Given
    UUID cardId = UUID.randomUUID();
    String errorMessage = "Card not found";

    when(cardService.getCardById(cardId)).thenThrow(new NotFoundException(errorMessage));

    // When && Then
    mockMvc.perform(get("/api/data/cards/{cardId}", cardId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).getCardById(cardId);
  }

  @Test
  @DisplayName("DELETE /api/data/cards/{cardId} - Should delete card return 200 OK")
  void deleteCard_shouldReturn200() throws Exception {
    // Given
    UUID cardId = UUID.randomUUID();

    doNothing().when(cardService).deleteCard(cardId);

    // When & Then
    mockMvc.perform(delete("/api/data/cards/{cardId}", cardId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Card deleted successfully."));
    verify(cardService).deleteCard(cardId);
  }

  @Test
  @DisplayName("DELETE /api/data/cards/{cardId} - Should return 404 when card not found")
  void deleteCard_cardNotFound_shouldReturn404() throws Exception {
    // Given
    UUID cardId = UUID.randomUUID();
    String errorMessage = "Card not found";

   doThrow(new NotFoundException(errorMessage)).when(cardService).deleteCard(cardId);

    // When && Then
    mockMvc.perform(delete("/api/data/cards/{cardId}", cardId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));
    verify(cardService).deleteCard(cardId);
  }

  @Test
  @DisplayName("PATCH /api/data/cards/{cardId}/{tagId} - Attach tag successfully and return 200 OK")
  void attachTag_shouldReturn200() throws Exception {
    // Given
    UUID cardId = UUID.randomUUID();
    UUID tagId = UUID.randomUUID();
    String successMessage = "Tag attached successfully.";

    doNothing().when(cardService).attachTag(cardId, tagId);

    // When & Then
    mockMvc.perform(patch("/api/data/cards/{cardId}/{tagId}", cardId, tagId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(successMessage));

    verify(cardService).attachTag(cardId, tagId);
  }

  @Test
  @DisplayName("PATCH /api/data/cards/{cardId}/{tagId} - Return 404 when Card is not found")
  void attachTag_cardNotFound_shouldReturn404() throws Exception {
    // Given
    UUID cardId = UUID.randomUUID();
    UUID tagId = UUID.randomUUID();
    String errorMessage = "The card not found";

    doThrow(new NotFoundException(errorMessage))
        .when(cardService).attachTag(cardId, tagId);

    // When & Then
    mockMvc.perform(patch("/api/data/cards/{cardId}/{tagId}", cardId, tagId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));

    verify(cardService).attachTag(cardId, tagId);
  }

  @Test
  @DisplayName("PATCH /api/data/cards/{cardId}/{tagId} - Return 404 when Tag is not found")
  void attachTag_tagNotFound_shouldReturn404() throws Exception {
    // Given
    UUID cardId = UUID.randomUUID();
    UUID tagId = UUID.randomUUID();
    String errorMessage = "Tag not found.";

    doThrow(new NotFoundException(errorMessage))
        .when(cardService).attachTag(cardId, tagId);

    // When & Then
    mockMvc.perform(patch("/api/data/cards/{cardId}/{tagId}", cardId, tagId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(errorMessage));

    verify(cardService).attachTag(cardId, tagId);
  }

}
