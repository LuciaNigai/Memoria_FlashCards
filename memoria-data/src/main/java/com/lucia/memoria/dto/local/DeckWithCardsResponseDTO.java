package com.lucia.memoria.dto.local;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeckWithCardsResponseDTO {

  private DeckResponseDTO deck;
  private List<CardResponseDTO> cards = new ArrayList<>();
}
