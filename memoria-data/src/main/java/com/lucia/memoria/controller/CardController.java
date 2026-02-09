package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.GeneralResponseDTO;
import com.lucia.memoria.service.local.CardService;
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

  @PostMapping
  public ResponseEntity<CardRequestDTO> createCard(@RequestBody CardRequestDTO cardDTO, @RequestParam(name = "saveDuplicate", defaultValue = "false") boolean saveDuplicate) {
    return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(cardDTO, saveDuplicate));
  }

  @PatchMapping("/{cardId}")
  public ResponseEntity<CardRequestDTO> updateCard(@PathVariable("cardId") UUID cardId, @RequestBody CardRequestDTO cardDTO, @RequestParam(name = "saveDuplicate", defaultValue = "false") boolean saveDuplicate) {
    return ResponseEntity.status(HttpStatus.CREATED).body(cardService.updateCard(cardId, cardDTO, saveDuplicate));
  }
  @GetMapping("/{cardId}")
  public ResponseEntity<CardResponseDTO> getCardById(@PathVariable("cardId") UUID cardId) {
    return ResponseEntity.ok().body(cardService.getCardById(cardId));
  }

  @DeleteMapping("/{cardId}")
  public ResponseEntity<GeneralResponseDTO<Void>> deleteCard(@PathVariable("cardId") UUID cardId) {
    cardService.deleteCard(cardId);
    return ResponseEntity.ok().body(new GeneralResponseDTO<>("Card deleted successfully."));
  }
}
