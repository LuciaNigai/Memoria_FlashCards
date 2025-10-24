package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.DeckDTO;
import com.lucia.memoria.dto.local.DeckMinimalDTO;
import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.dto.local.RenameRequestDTO;
import com.lucia.memoria.dto.local.ResponseDeckWithCardsDTO;
import com.lucia.memoria.service.local.CardService;
import com.lucia.memoria.service.local.DeckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data/decks")
public class DeckController {

  private final DeckService deckService;
  private final CardService cardService;

  public DeckController(DeckService deckService, CardService cardService) {
    this.deckService = deckService;
    this.cardService = cardService;
  }

  @PostMapping
  public ResponseEntity<DeckDTO> createDeck(@RequestBody DeckMinimalDTO deckMinimalDTO) {
    return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeck(deckMinimalDTO));
  }

  @GetMapping("/{deckId}/cards")
  public ResponseEntity<ResponseDeckWithCardsDTO> getDeckWithCards(
      @PathVariable("deckId") UUID deckId) {
    return ResponseEntity.ok().body(cardService.getDeckWithCards(deckId));
  }

  @GetMapping("/{deckId}")
  public ResponseEntity<DeckDTO> getDeckById(@PathVariable("deckId") UUID deckId) {
    return ResponseEntity.ok().body(deckService.getDeckById(deckId));
  }

  @DeleteMapping("/{deckId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteDeck(@PathVariable("deckId") UUID deckId,
      @RequestParam(name = "force", defaultValue = "false") boolean force) {
    deckService.deleteDeck(deckId, force);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Deck deleted successfully."));
  }

  @PatchMapping("/{deckId}")
  public ResponseEntity<DeckMinimalDTO> renameDeck(@PathVariable("deckId") UUID deckId, @RequestBody
  RenameRequestDTO newName) {
    return ResponseEntity.ok().body(deckService.renameDeck(deckId, newName.name()));
  }
}
