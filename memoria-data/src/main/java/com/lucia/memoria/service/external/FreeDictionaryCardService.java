package com.lucia.memoria.service.external;

import com.lucia.memoria.dto.externalapi.DefinitionDTO;
import com.lucia.memoria.dto.externalapi.MeaningDTO;
import com.lucia.memoria.dto.externalapi.ResponseDTO;
import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.FieldRequestDTO;
import com.lucia.memoria.dto.local.TemplateFieldRequestDTO;
import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.TemplateField;
import com.lucia.memoria.service.local.TemplateService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class FreeDictionaryCardService {

  private final FreeDictionaryAPIService freeDictionaryAPIService;
  private final TemplateService templateService;


  public List<CardRequestDTO> generateCards(String word) {
    Template template = templateService.getTemplateByName("default");
    List<TemplateField> templateFields = template.getFields();

    return freeDictionaryAPIService.callExternalApi(word)
        .map(externalList -> externalList.stream()
            .flatMap(
                resp -> constructCardDTOs(resp, template, templateFields).stream()) // flatten here
            .toList()
        )
        .block();
  }

  private List<CardRequestDTO> constructCardDTOs(ResponseDTO resp, Template template,
      List<TemplateField> templateFields) {
    List<MeaningDTO> meanings = resp.meanings() != null ? resp.meanings() : Collections.emptyList();
//    safeguard if the word will contain more options for the same part of speech
    Map<String, List<DefinitionDTO>> words = meanings.stream()
        .collect(Collectors.toMap(
            MeaningDTO::partOfSpeech,
            MeaningDTO::definitions,
            (existingList, newList) -> {
              // merge two lists
              List<DefinitionDTO> merged = new ArrayList<>(existingList);
              merged.addAll(newList);
              return merged;
            }
        ));

    List<CardRequestDTO> cards = new ArrayList<>();
    for (String partOfSpeech : words.keySet()) {
      List<DefinitionDTO> definitions = words.get(partOfSpeech);
      cards.add(constructCardDTO(resp, template, templateFields, partOfSpeech, definitions));
    }
    return cards;
  }

  private static CardRequestDTO constructCardDTO(ResponseDTO resp, Template template,
      List<TemplateField> templateFields, String partOfSpeech, List<DefinitionDTO> definitionDTOS) {
    List<FieldRequestDTO> cardFields = templateFields.stream().map(fieldTemplate ->
    {
      FieldRole templateFieldRole = fieldTemplate.getFieldRole();
      TemplateFieldRequestDTO templateFieldRequestDTO = new TemplateFieldRequestDTO(
          fieldTemplate.getTemplateFieldId(), fieldTemplate.getName(), fieldTemplate.getFieldRole(),
          fieldTemplate.getTemplateFieldType());
      if (templateFieldRole == FieldRole.FRONT) {
        String frontContent = resp.word();
        if (resp.phonetic() != null && !resp.phonetic().isEmpty()) {
          frontContent += "\nPronunciation: " + resp.phonetic();
        }
        return new FieldRequestDTO(frontContent, templateFieldRequestDTO.getId());
      } else if (templateFieldRole == FieldRole.BACK) {
        String definitions = definitionDTOS.isEmpty() ? "No definitions available."
            : formatDefinitions(definitionDTOS);
        return new FieldRequestDTO(definitions, templateFieldRequestDTO.getId());
      } else if (templateFieldRole == FieldRole.AUXILIARY) {
        return new FieldRequestDTO(partOfSpeech, templateFieldRequestDTO.getId());
      }
      throw new IllegalArgumentException("Error formating response");
    }).toList();

    return new CardRequestDTO(null, null, template.getTemplateId(), cardFields);
  }

  private static String formatDefinitions(List<DefinitionDTO> definitions) {
    return definitions.stream()
        .map(d -> {
          String definition = d.definition() != null ? " Definition: " + d.definition() : "";
          String synonyms = d.synonyms() != null ? " Synonyms: " + d.synonyms() : "";
          String antonyms = d.antonyms() != null ? " Antonyms: " + d.antonyms() : "";
          String examplePart = d.example() != null ? " Example: " + d.example() : "";
          return definition + "\n" + synonyms + "\n" + antonyms + "\n" + examplePart;
        })
        .collect(Collectors.joining("\n"));
  }
}
