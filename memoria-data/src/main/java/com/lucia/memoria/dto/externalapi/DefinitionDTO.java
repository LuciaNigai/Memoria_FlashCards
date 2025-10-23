package com.lucia.memoria.dto.externalapi;

import java.util.List;

public record DefinitionDTO(String definition, List<String> synonyms, List<String> antonyms,
                            String example) {

}

