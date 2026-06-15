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
public class ResponseDeckWithCardsDTO {

  private DeckRequestDTO deck;
  private List<CardRequestDTO> cards = new ArrayList<>();
}
