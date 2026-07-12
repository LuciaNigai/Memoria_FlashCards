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
import com.lucia.memoria.controller.DeckController;
import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.DeckRequestDTO;
import com.lucia.memoria.dto.local.DeckResponseDTO;
import com.lucia.memoria.dto.local.DeckWithCardsResponseDTO;
import com.lucia.memoria.dto.local.RenameRequestDTO;
import com.lucia.memoria.exception.ConflictWithDataException;
import com.lucia.memoria.exception.NotFoundException;
import com.lucia.memoria.service.local.CardService;
import com.lucia.memoria.service.local.DeckService;
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
@WebMvcTest(DeckController.class)
class DeckControllerWebSliceTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private DeckService deckService;
  @MockitoBean
  private CardService cardService;

  @Test
  @DisplayName("POST /api/data/decks - Should create a new deck and return 201 Created")
  void createDeck_shouldReturn201() throws Exception {
    // Given
    String deckName = "Spanish";
    DeckRequestDTO requestDTO = new DeckRequestDTO();
    requestDTO.setName(deckName);
    requestDTO.setUserId(UUID.randomUUID());
    UUID deckId = UUID.randomUUID();
    DeckResponseDTO responseDTO = new DeckResponseDTO();
    responseDTO.setId(deckId);
    responseDTO.setName(deckName);

    when(deckService.createDeck(any(DeckRequestDTO.class))).thenReturn(responseDTO);

    // When & Then
    mockMvc.perform(post("/api/data/decks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(deckId.toString()))
        .andExpect(jsonPath("$.name").value(deckName));

    verify(deckService).createDeck(any(DeckRequestDTO.class));
  }

  @Test
  @DisplayName("POST /api/data/decks - Should return 400 Bad Request when deck name is blank")
  void createDeck_withBlankName_shouldReturn400BadRequest() throws Exception {
    // Given
    DeckRequestDTO deckRequestDTO = new DeckRequestDTO();
    deckRequestDTO.setUserId(UUID.randomUUID());

    // When & Then
    mockMvc.perform(post("/api/data/decks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deckRequestDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0]").value("name cannot be empty"));
    ;
  }

  @Test
  @DisplayName("POST /api/data/decks - Should return 400 Bad Request when user id is blank")
  void createDeck_withBlankUserId_shouldReturn400BadRequest() throws Exception {
    // Given
    DeckRequestDTO deckRequestDTO = new DeckRequestDTO();
    deckRequestDTO.setName("Deck name");

    // When & Then
    mockMvc.perform(post("/api/data/decks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deckRequestDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0]").value("userId should be present"));
  }

  @Test
  @DisplayName("GET /api/data/decks/{deckId}/cards - Should return deck with cards and 200 OK")
  void getDecksWithCards_shouldReturn200() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    DeckResponseDTO deckResponseDTO = new DeckResponseDTO();
    deckResponseDTO.setId(deckId);
    deckResponseDTO.setName("Spanish");
    UUID cardId = UUID.randomUUID();
    CardResponseDTO cardResponseDTO = new CardResponseDTO();
    cardResponseDTO.setDeckId(cardId);
    cardResponseDTO.setId(cardId);

    DeckWithCardsResponseDTO deckWithCardsResponseDTO = new DeckWithCardsResponseDTO();
    deckWithCardsResponseDTO.setDeck(deckResponseDTO);
    deckWithCardsResponseDTO.setCards(List.of(cardResponseDTO));

    when(cardService.getDeckWithCards(deckId)).thenReturn(deckWithCardsResponseDTO);

    //When & Then
    mockMvc.perform(get("/api/data/decks/{deckId}/cards", deckId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deck.id").value(deckId.toString()))
        .andExpect(jsonPath("$.deck.name").value("Spanish"))
        .andExpect(jsonPath("$.cards").isArray())
        .andExpect(jsonPath("$.cards[0].id").value(cardId.toString()));

    verify(cardService).getDeckWithCards(deckId);
  }

  @Test
  @DisplayName("GET /api/data/decks/{deckId}/cards - Should return 404 not found when deck does not exist")
  void getDeckWithCards_deckDoesNotExist_shouldReturn404NotFound() throws Exception {
    //Given
    UUID deckId = UUID.randomUUID();

    when(cardService.getDeckWithCards(deckId))
        .thenThrow(new NotFoundException("Deck not found"));

    // When & Then
    mockMvc.perform(get("/api/data/decks/{deckId}/cards", deckId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/data/decks/{deckId} - Should return deck without cards and 200 OK")
  void getDeckById_shouldReturn200() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    String deckName = "Spanish";
    DeckResponseDTO deckResponseDTO = new DeckResponseDTO();
    deckResponseDTO.setId(deckId);
    deckResponseDTO.setName(deckName);

    when(deckService.getDeckById(deckId)).thenReturn(deckResponseDTO);

    // When & Then
    mockMvc.perform(get("/api/data/decks/{deckId}", deckId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(deckId.toString()))
        .andExpect(jsonPath("$.name").value(deckName));

    verify(deckService).getDeckById(deckId);
  }

  @Test
  @DisplayName("GET /api/data/decks/{deckId} - Should return 404 not found when deck does not exist")
  void getDeckById_deckDoesNotExist_shouldReturn404NotFound() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();

    when(deckService.getDeckById(deckId))
        .thenThrow(new NotFoundException("Deck not found"));

    // When & Then
    mockMvc.perform(get("/api/data/decks/{deckId}", deckId))
        .andExpect(status().isNotFound());
    verify(deckService).getDeckById(deckId);
  }


  @Test
  @DisplayName("DELETE /api/data/decks/{deckId} - Should delete deck with default false parameter and return 200 OK")
  void deleteDeck_defaultFalseParam_shouldReturn200() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();

    doNothing().when(deckService).deleteDeck(deckId, false);

    // When & Then
    mockMvc.perform(delete("/api/data/decks/{deckId}", deckId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Deck deleted successfully."));
    verify(deckService).deleteDeck(deckId, false);
  }

  @Test
  @DisplayName("DELETE /api/data/decks/{deckId}?force=true - Should pass parameter as true")
  void deleteDeck_withForceTrue_shouldReturn200() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();

    doNothing().when(deckService).deleteDeck(deckId, true);

    // When & Then
    mockMvc.perform(delete("/api/data/decks/{deckId}", deckId)
            .param("force", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Deck deleted successfully."));
    verify(deckService).deleteDeck(deckId, true);
  }

  @Test
  @DisplayName("DELETE /api/data/decks/{deckId} - Should return 404 not found when deck does not exist")
  void deleteDeck_deckDoesNotExist_shouldReturn404NotFound() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();

    doThrow(new NotFoundException("Deck not found")).when(deckService).deleteDeck(deckId, false);

    // When & Then
    mockMvc.perform(delete("/api/data/decks/{deckId}", deckId))
        .andExpect(status().isNotFound());
    verify(deckService).deleteDeck(deckId, false);
  }

  @Test
  @DisplayName("DELETE /api/data/decks/{deckId} - Should return 409 Conflict if deck is not empty and force is false")
  void deleteDeck_notEmpty_shouldReturn409Conflict() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();

    doThrow(new ConflictWithDataException(
        "Cannot delete deck(s) containing cards without force flag.")).when(deckService)
        .deleteDeck(deckId, false);

    //When & Then
    mockMvc.perform(delete("/api/data/decks/{deckId}", deckId))
        .andExpect(status().isConflict());
    verify(deckService).deleteDeck(deckId, false);
  }


  @Test
  @DisplayName("PATCH /api/data/decks/{deckId} - Should rename deck and return 200 OK")
  void renameDeck_shouldReturn200Ok() throws Exception {
    // GIven
    UUID deckId = UUID.randomUUID();
    String newName = "New Deck Name";
    RenameRequestDTO renameRequestDTO = new RenameRequestDTO(newName);

    DeckResponseDTO deckResponseDTO = new DeckResponseDTO();
    deckResponseDTO.setId(deckId);
    deckResponseDTO.setName(newName);

    when(deckService.renameDeck(deckId, newName)).thenReturn(deckResponseDTO);

    // When & Then
    mockMvc.perform(patch("/api/data/decks/{deckId}", deckId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(renameRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(deckId.toString()))
        .andExpect(jsonPath("$.name").value(newName));
    verify(deckService).renameDeck(deckId, newName);
  }

  @Test
  @DisplayName("PATCH /api/data/decks/{deckId} - Should return 404 not found when deck does not exist")
  void renameDeck_deckDoesNotExist_shouldReturn404NotFound() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    String newDeckName = "New deck name";
    RenameRequestDTO renameRequestDTO = new RenameRequestDTO(newDeckName);

    when(deckService.renameDeck(eq(deckId), any(String.class)))
        .thenThrow(new NotFoundException("Deck not found."));

    // When & Then
    mockMvc.perform(patch("/api/data/decks/{deckId}", deckId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(renameRequestDTO)))
        .andExpect(status().isNotFound());
    verify(deckService).renameDeck(deckId, newDeckName);
  }

  @Test
  @DisplayName("PATCH /api/data/decks/{deckId} - Should return 400 Bad Request when name is blank")
  void renameDeck_blankName_shouldReturn400BadRequest() throws Exception {
    // Given
    UUID deckId = UUID.randomUUID();
    RenameRequestDTO renameRequestDTO = new RenameRequestDTO("");

    // When & Then
    mockMvc.perform(patch("/api/data/decks/{deckId}", deckId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(renameRequestDTO)))
        .andExpect(status().isBadRequest());
  }
}
