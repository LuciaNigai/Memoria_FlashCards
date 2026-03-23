package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.service.local.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data/cards")
public class CardController {

  private final CardService cardService;

  public CardController(CardService cardService) {
    this.cardService = cardService;
  }


  @Tag(name = "create")
  @Operation(summary = "Create new card", description = "Creates a new card. Optionally allows saving duplicates.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created card", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Field's template field not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "409", description = "Conflict – invalid card or template configuration", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @PostMapping
  public ResponseEntity<CardResponseDTO> createCard(@RequestBody CardRequestDTO cardDTO,
      @Parameter(description = "Allow saving duplicate cards")
      @RequestParam(name = "saveDuplicate", defaultValue = "false") boolean saveDuplicate) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(cardService.createCard(cardDTO, saveDuplicate));
  }

  @Tag(name = "update")
  @Operation(summary = "Update card", description = "Updates a card based on id. Optionally allows saving duplicates.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated card", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Card not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "409", description = "Conflict – invalid card or template configuration", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @PatchMapping("/{cardId}")
  public ResponseEntity<CardResponseDTO> updateCard(@PathVariable("cardId") UUID cardId,
      @RequestBody CardRequestDTO cardDTO,
      @Parameter(description = "Allow saving duplicate cards")
      @RequestParam(name = "saveDuplicate", defaultValue = "false") boolean saveDuplicate) {
    return ResponseEntity.ok()
        .body(cardService.updateCard(cardId, cardDTO, saveDuplicate));
  }

  @Tag(name = "find")
  @Operation(summary = "Get card by id", description = "Returns a card by its unique identifier.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Returns card based on id", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Card not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @GetMapping("/{cardId}")
  public ResponseEntity<CardResponseDTO> getCardById(@PathVariable("cardId") UUID cardId) {
    return ResponseEntity.ok().body(cardService.getCardById(cardId));
  }

  @Tag(name = "delete")
  @Operation(summary = "Delete card", description = "Delete the card by it's unique identifier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Deletes card", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Card not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @DeleteMapping("/{cardId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteCard(@PathVariable("cardId") UUID cardId) {
    cardService.deleteCard(cardId);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Card deleted successfully."));
  }
}
