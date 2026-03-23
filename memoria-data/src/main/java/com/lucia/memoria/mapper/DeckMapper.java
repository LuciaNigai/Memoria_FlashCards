package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.DeckRequestDTO;
import com.lucia.memoria.dto.local.DeckResponseDTO;
import com.lucia.memoria.model.Deck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeckMapper {

  @Mapping(target = "userId", source = "user.userId")
  DeckResponseDTO toDTO(Deck deck);

  @Mapping(target = "userId", source = "user.userId")
  DeckRequestDTO toMinimalDTO(Deck deck);
}
