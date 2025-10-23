package com.lucia.memoria.service.external;

import com.lucia.memoria.config.ExternalAPIConfig;
import com.lucia.memoria.dto.externalapi.ResponseDTO;
import com.lucia.memoria.exception.ClientApiException;
import com.lucia.memoria.exception.ServerApiException;
import java.time.Duration;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class FreeDictionaryAPIService {

  private final WebClient webClient;

  public FreeDictionaryAPIService(WebClient.Builder webClientBuilder,
      ExternalAPIConfig externalAPIConfig) {
    this.webClient = webClientBuilder.baseUrl(externalAPIConfig.getFreeDictionary()).build();
  }

  public Mono<List<ResponseDTO>> callExternalApi(String word) {
    return webClient.get()
        .uri("/{word}", word)
        .retrieve()
        // Handle 4xx client errors
        .onStatus(HttpStatusCode::is4xxClientError, response ->
            response.bodyToMono(String.class)
                .defaultIfEmpty("Client error without body")
                .flatMap(body -> Mono.error(new ClientApiException(
                    "Client Error: " + response.statusCode() + ", Body: " + body))))
        // Handle 5xx server errors
        .onStatus(HttpStatusCode::is5xxServerError, response ->
            response.bodyToMono(String.class)
                .defaultIfEmpty("Server error without body")
                .flatMap(body -> Mono.error(new ServerApiException(
                    "Server Error: " + response.statusCode() + ", Body: " + body))))
        .bodyToMono(new ParameterizedTypeReference<List<ResponseDTO>>() {
        })
        .timeout(Duration.ofSeconds(5))
        // Retry on server errors with exponential backoff
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
            .maxBackoff(Duration.ofSeconds(10))
            .filter(throwable -> throwable instanceof ServerApiException)
            .onRetryExhaustedThrow((spec, signal) ->
                new ServerApiException("Retries exhausted for word: " + word, signal.failure())))
        // Optional: fallback to empty list if server error persists
        .onErrorResume(ServerApiException.class, ex -> {
          log.warn("Falling back due to server error for word '{}': {}", word, ex.getMessage());
          return Mono.just(Collections.emptyList());
        })
        .doOnError(throwable ->
            log.error("Error calling external API for word '{}'", word, throwable));
  }
}
