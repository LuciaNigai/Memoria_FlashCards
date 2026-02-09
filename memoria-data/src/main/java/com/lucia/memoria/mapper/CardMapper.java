package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.model.Card;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.Template;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {FieldMapper.class})
public interface CardMapper {

  Card toEntity(CardResponseDTO cardResponseDTO);

  @Mapping(target = "templateId", source = "template", qualifiedByName = "templateToTemplateId")
  @Mapping(target = "deckId", source = "deck", qualifiedByName = "deckToDeckId")
  @Mapping(target = "fieldRequestDTOList", source = "fields")
  CardRequestDTO toMinimalDTO(Card card);

  @Mapping(target = "templateId", source = "template", qualifiedByName = "templateToTemplateId")
  @Mapping(target = "deckId", source = "deck", qualifiedByName = "deckToDeckId")
  @Mapping(target = "fieldRequestDTOList", source = "fields")
  List<CardRequestDTO> toMinimalDTOList(List<Card> cards);

  @Mapping(target = "templateId", source = "template", qualifiedByName = "templateToTemplateId")
  @Mapping(target = "fieldDTOList", ignore = true)
  @Mapping(target = "deckId", source = "deck", qualifiedByName = "deckToDeckId")
  CardResponseDTO toDTO(Card card);

  @Named("templateToTemplateId")
  default UUID mapTemplateToTemplateId(Template template) {
    return template == null ? null : template.getTemplateId();
  }

  @Named("deckToDeckId")
  default UUID mapDeckToDeckId(Deck deck) {
    return deck == null ? null : deck.getDeckId();
  }
}
