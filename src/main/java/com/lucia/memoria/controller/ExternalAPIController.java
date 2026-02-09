package com.lucia.memoria.controller;

import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.service.external.FreeDictionaryCardService;
import com.lucia.memoria.service.external.GoogleAPIService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/data/search")
@AllArgsConstructor
public class ExternalAPIController {

  private final FreeDictionaryCardService freeDictionaryCardService;
  private final GoogleAPIService googleAPIService;


  @GetMapping("/meaning/{word}")
  public ResponseEntity<List<CardResponseDTO>> getWordDefinition(@PathVariable("word") String word) {
    return ResponseEntity.ok().body(freeDictionaryCardService.generateCards(word));
  }

  @GetMapping("/translation/{source}/{target}/{word}")
  public Mono<String> getWordDefinition(@PathVariable("source") String source,
      @PathVariable("target") String target, @PathVariable("word") String word) {
    return googleAPIService.callExternalApi(source, target, word);
  }
}
