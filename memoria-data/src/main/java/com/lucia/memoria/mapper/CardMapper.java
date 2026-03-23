package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.CardResponseDTO;
import com.lucia.memoria.dto.local.CardRequestDTO;
import com.lucia.memoria.model.Card;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {FieldMapper.class})
public interface CardMapper {

  @Mapping(target = "templateId", source = "template", qualifiedByName = "templateToTemplateId")
  @Mapping(target = "deckId", source = "deck", qualifiedByName = "deckToDeckId")
  @Mapping(target = "fieldRequestDTOList", source = "fields")
  CardRequestDTO toMinimalDTO(Card card);

  @Mapping(target = "templateId", source = "template", qualifiedByName = "templateToTemplateId")
  @Mapping(target = "deckId", source = "deck", qualifiedByName = "deckToDeckId")
  @Mapping(target = "fieldRequestDTOList", source = "fields")
  List<CardRequestDTO> toMinimalDTOList(List<Card> cards);

  @Mapping(target = "templateId", source = "template", qualifiedByName = "templateToTemplateId")
  @Mapping(target = "fieldDTOList", source = "fields")
  @Mapping(target = "deckId", source = "deck", qualifiedByName = "deckToDeckId")
  CardResponseDTO toDTO(Card card);
}
