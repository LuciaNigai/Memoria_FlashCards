package com.lucia.memoria.service.external;

import com.lucia.memoria.dto.externalapi.DefinitionDTO;
import com.lucia.memoria.dto.externalapi.MeaningDTO;
import com.lucia.memoria.dto.externalapi.ResponseDTO;
import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.FieldRequestDTO;
import com.lucia.memoria.dto.local.TemplateFieldRequestDTO;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.service.local.DeckService;
import com.lucia.memoria.service.local.TemplateService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FreeDictionaryCardService {

  private final FreeDictionaryAPIService freeDictionaryAPIService;
  private final TemplateService templateService;
  private final DeckService deckService;

  @Value("${app.templates.default-dictionary-name:Default}")
  private String defaultTemplateName;

  @Value("${app.decks.default-deck-name:Default}")
  private String defaultDeckName;

  /**
   * Main entry point: Calls the external FreeDictionary API for a given word and maps the raw API
   * response into a list of populated CardRequestDTOs.
   */
  public List<CardRequestDTO> generateCards(String word) {
    // Step 1: Fetch the user's default template schema to know what fields to populate
    Template template = templateService.getTemplateByName(defaultTemplateName);
    Deck defaultDeck = deckService.getDeckByName(defaultDeckName);

    List<TemplateField> templateFields = template.getFields();
    UUID deckId = defaultDeck != null ? defaultDeck.getDeckId() : null;

    // Step 2: Make the reactive WebClient API call
    return freeDictionaryAPIService.callExternalApi(word)
        // Step 3: Map each API response object into card DTOs and flatten the resulting lists
        .map(externalList -> externalList.stream()
            .flatMap(resp -> constructCardDTOs(resp, template, deckId, templateFields).stream())
            .toList()
        )
        .block(); // Step 4: Block to synchronously unwrap the Mono<List<CardRequestDTO>> into List<CardRequestDTO>
  }

  /**
   * Transforms a single API dictionary response into multiple cards grouped by part of speech.
   */
  private List<CardRequestDTO> constructCardDTOs(
      ResponseDTO resp,
      Template template,
      UUID deckId,
      List<TemplateField> templateFields
  ) {
    // Step 1: Ensure meanings list is non-null
    List<MeaningDTO> meanings = resp.meanings() != null ? resp.meanings() : Collections.emptyList();

    // Step 2: Group definitions by part of speech (e.g., "noun" -> [def1, def2], "verb" -> [def1])
    Map<String, List<DefinitionDTO>> wordsByPos = meanings.stream()
        .collect(Collectors.toMap(
            MeaningDTO::partOfSpeech,
            m -> m.definitions() != null ? m.definitions() : Collections.emptyList(),
            // Merge function: If the API returns multiple entries for the same part of speech, combine their definitions
            (existingList, newList) -> {
              List<DefinitionDTO> merged = new ArrayList<>(existingList);
              merged.addAll(newList);
              return merged;
            }
        ));

    // Step 3: Build one card DTO per unique part of speech group
    return wordsByPos.entrySet().stream()
        .map(entry -> constructCardDTO(resp, template, deckId, templateFields, entry.getKey(),
            entry.getValue()))
        .toList();
  }

  /**
   * Constructs a single CardRequestDTO for a specific part of speech, matching content to each
   * template field.
   */
  private static CardRequestDTO constructCardDTO(
      ResponseDTO resp,
      Template template,
      UUID deckId,
      List<TemplateField> templateFields,
      String partOfSpeech,
      List<DefinitionDTO> definitions
  ) {
    // Step 1: Extract the primary (top-ranked) definition and example from the list
    DefinitionDTO primaryDef =
        (definitions != null && !definitions.isEmpty()) ? definitions.getFirst() : null;
    String definitionText =
        (primaryDef != null && primaryDef.definition() != null) ? primaryDef.definition().trim()
            : "";
    String exampleText =
        (primaryDef != null && primaryDef.example() != null) ? primaryDef.example().trim() : "";

    // Step 2: Map each field in the card template to its corresponding content string
    List<FieldRequestDTO> cardFields = templateFields.stream()
        .map(fieldTemplate -> {
          FieldRole role = fieldTemplate.getFieldRole();
          String fieldName =
              fieldTemplate.getName() != null ? fieldTemplate.getName().toLowerCase() : "";

          TemplateFieldRequestDTO templateFieldDTO = new TemplateFieldRequestDTO(
              fieldTemplate.getTemplateFieldId(),
              fieldTemplate.getName(),
              role
          );

          // Resolve the textual value that belongs in this field
          String content = resolveSingleFieldContent(role, fieldName, resp, partOfSpeech,
              definitionText, exampleText);
          return new FieldRequestDTO(content, templateFieldDTO.getId());
        })
        .toList();

    // Step 3: Return the assembled card request DTO linked to its template ID
    return new CardRequestDTO(null, deckId, template.getTemplateId(), cardFields);
  }

  /**
   * Determines what text content populates a specific field based on field name keywords or field role fallback.
   */
  private static String resolveSingleFieldContent(
      FieldRole role,
      String fieldName,
      ResponseDTO resp,
      String partOfSpeech,
      String definitionText,
      String exampleText
  ) {
    // Strategy 1: Priority routing by Field Name (allows fine-grained field matching)
    if (fieldName.contains("transcription") || fieldName.contains("phonetic") || fieldName.contains(
        "pronunciation")) {
      return resp.phonetic() != null ? resp.phonetic() : "";
    }
    if (fieldName.contains("example")) {
      return exampleText;
    }
    if (fieldName.contains("part") || fieldName.contains("pos") || fieldName.contains("speech")) {
      return partOfSpeech != null ? partOfSpeech : "";
    }
    if (fieldName.contains("definition") || fieldName.contains("translation")) {
      return definitionText;
    }

    // Strategy 2: Fallback routing by FieldRole when name matching isn't explicit
    if (role == null) {
      return "";
    }

    return switch (role) {
      case FRONT -> resp.word() != null ? resp.word() : "";
      case BACK -> definitionText;
      case AUXILIARY -> partOfSpeech != null ? partOfSpeech : "";
      default -> "";
    };
  }
}