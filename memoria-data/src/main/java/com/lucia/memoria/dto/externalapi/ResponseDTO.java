package com.lucia.memoria.dto.externalapi;

import java.util.List;

public record ResponseDTO(String word, String phonetic, List<MeaningDTO> meanings) {

}

