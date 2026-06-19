package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.DeckWithCardsResponseDTO;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Deck;
import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = {DeckMapper.class, CardMapper.class})
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

  public DeckWithCardsResponseDTO toDTO(Deck deck, List<Card> cards) {
    if (deck == null) return null;

    DeckWithCardsResponseDTO dto = new DeckWithCardsResponseDTO();
    dto.setDeck(deckMapper.toDTO(deck));

    if (cards != null) {
      dto.setCards(cards.stream()
          .map(cardMapper::toResponseDTO)
          .toList());
    }

    return dto;
  }
}
