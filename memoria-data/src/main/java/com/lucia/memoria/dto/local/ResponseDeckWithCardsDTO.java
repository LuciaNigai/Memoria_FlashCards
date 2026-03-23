package com.lucia.memoria.dto.local;

import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDeckWithCardsDTO {

  private DeckRequestDTO deck;
  private List<CardRequestDTO> cards = new ArrayList<>();
}
