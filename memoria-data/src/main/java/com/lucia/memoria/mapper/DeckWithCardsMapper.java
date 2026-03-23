package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.ResponseDeckWithCardsDTO;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Deck;
import org.mapstruct.Mapper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {DeckMapper.class, CardMapper.class})
public abstract class DeckWithCardsMapper {

  private DeckMapper deckMapper;
  private CardMapper cardMapper;

  @Autowired
  public void setDeckMapper(DeckMapper deckMapper) {
    this.deckMapper = deckMapper;
  }

  @Autowired
  public void setCardMapper(CardMapper cardMapper) {
    this.cardMapper = cardMapper;
  }

  public ResponseDeckWithCardsDTO toDTO(Deck deck, List<Card> cards) {
    if (deck == null) return null;

    ResponseDeckWithCardsDTO dto = new ResponseDeckWithCardsDTO();
    dto.setDeck(deckMapper.toMinimalDTO(deck));

    if (cards != null) {
      dto.setCards(cards.stream()
          .map(cardMapper::toRequestDTO)
          .toList());
    }

    return dto;
  }
}
