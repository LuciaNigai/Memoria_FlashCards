package com.lucia.memoria.service.external;

import com.lucia.memoria.config.ExternalAPIConfig;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class GoogleAPIService {

  private final WebClient webClient;

  public GoogleAPIService(WebClient.Builder webClientBuilder, ExternalAPIConfig externalAPIConfig) {
    this.webClient = webClientBuilder.baseUrl(externalAPIConfig.getGoogleTranslate()).build();
  }

  public Mono<String> callExternalApi(String source, String target, String word) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/translate_a/single")
            .queryParam("client", "gtx")
            .queryParam("sl", source)
            .queryParam("tl", target)
            .queryParam("dt", "t")
            .queryParam("q", word)
            .build())
        .retrieve()
        .bodyToMono(List.class)
        .map(response -> ((List<List<String>>) (response.getFirst())).getFirst().getFirst());
  }
}
