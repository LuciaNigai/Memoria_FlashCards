package com.lucia.memoria.dto.externalapi;

import java.util.List;

public record MeaningDTO(String partOfSpeech, List<DefinitionDTO> definitions) {

}
