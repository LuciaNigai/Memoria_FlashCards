package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.DeckRequestDTO;
import com.lucia.memoria.dto.local.DeckResponseDTO;
import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.dto.local.RenameRequestDTO;
import com.lucia.memoria.dto.local.ResponseDeckWithCardsDTO;
import com.lucia.memoria.service.local.CardService;
import com.lucia.memoria.service.local.DeckService;
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
@RequestMapping("/api/data/decks")
@Tag(name = "Decks", description = "Endpoints for managing decks")
public class DeckController {

  private final DeckService deckService;
  private final CardService cardService;

  public DeckController(DeckService deckService, CardService cardService) {
    this.deckService = deckService;
    this.cardService = cardService;
  }

  @Operation(summary = "Create new deck", description = "Creates a new deck for the user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created deck", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DeckResponseDTO.class))
      }),
      @ApiResponse(responseCode = "400", description = "Invalid input", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @PostMapping
  public ResponseEntity<DeckResponseDTO> createDeck(@RequestBody DeckRequestDTO deckRequestDTO) {
    return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeck(deckRequestDTO));
  }

  @Operation(summary = "Get deck with cards", description = "Returns a deck and all its associated cards.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Found deck with cards", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDeckWithCardsDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Deck not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @GetMapping("/{deckId}/cards")
  public ResponseEntity<ResponseDeckWithCardsDTO> getDeckWithCards(@PathVariable("deckId") UUID deckId) {
    return ResponseEntity.ok().body(cardService.getDeckWithCards(deckId));
  }

  @Operation(summary = "Get deck by id", description = "Returns basic information about a deck.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Found deck", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DeckResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Deck not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @GetMapping("/{deckId}")
  public ResponseEntity<DeckResponseDTO> getDeckById(@PathVariable("deckId") UUID deckId) {
    return ResponseEntity.ok().body(deckService.getDeckById(deckId));
  }

  @Operation(summary = "Delete deck", description = "Deletes a deck. If force is true, deletes all associated cards.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Deck deleted", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Deck not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      }),
      @ApiResponse(responseCode = "409", description = "Deck is not empty and force is false", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @DeleteMapping("/{deckId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteDeck(@PathVariable("deckId") UUID deckId,
      @Parameter(description = "Force delete if deck is not empty")
      @RequestParam(name = "force", defaultValue = "false") boolean force) {
    deckService.deleteDeck(deckId, force);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Deck deleted successfully."));
  }

  @Operation(summary = "Rename deck", description = "Updates the name of an existing deck.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Deck renamed", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = DeckRequestDTO.class))
      }),
      @ApiResponse(responseCode = "404", description = "Deck not found", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralResponseDTO.class))
      })
  })
  @PatchMapping("/{deckId}")
  public  ResponseEntity<DeckRequestDTO> renameDeck(@PathVariable("deckId") UUID deckId, @RequestBody
      RenameRequestDTO newName) {
    return ResponseEntity.ok().body(deckService.renameDeck(deckId, newName.name()));
  }
}
