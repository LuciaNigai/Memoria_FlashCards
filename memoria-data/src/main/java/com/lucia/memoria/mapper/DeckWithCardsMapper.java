package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.ResponseDeckWithCardsDTO;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Deck;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeckMapper.class, CardMapper.class})
@RequiredArgsConstructor
public abstract class DeckWithCardsMapper {

  protected DeckMapper deckMapper;
  protected CardMapper cardMapper;

  public ResponseDeckWithCardsDTO toDTO(Deck deck, List<Card> cards) {
    ResponseDeckWithCardsDTO dto = new ResponseDeckWithCardsDTO();
    dto.setDeck(deckMapper.toMinimalDTO(deck));
    dto.setCards(cards.stream()
        .map(cardMapper::toMinimalDTO)
        .toList());

    return dto;
  }
}
