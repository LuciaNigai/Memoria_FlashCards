package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.model.Card;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {FieldMapper.class})
public interface CardMapper {

  @Mapping(target = "templateId", source = "template.templateId")
  @Mapping(target = "deckId", source = "deck.deckId")
  CardRequestDTO toRequestDTO(Card card);

  @Mapping(target = "templateId", source = "template.templateId")
  @Mapping(target = "deckId", source = "deck.deckId")
  List<CardRequestDTO> toRequestDTOList(List<Card> cards);

  @Mapping(target = "templateId", source = "template.templateId")
  @Mapping(target = "deckId", source = "deck.deckId")
  CardResponseDTO toResponseDTO(Card card);
}
