package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.DeckRequestDTO;
import com.lucia.memoria.dto.local.DeckResponseDTO;
import com.lucia.memoria.model.Deck;
import com.lucia.memoria.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeckMapper {

  @Mapping(target = "userId", source = "user", qualifiedByName = "userToUserId")
  DeckResponseDTO toDTO(Deck deck);

  @Mapping(target = "userId", source = "user", qualifiedByName = "userToUserId")
  DeckRequestDTO toMinimalDTO(Deck deck);

  Deck toEntityFromMinimal(DeckRequestDTO deckRequestDTO);

  @Named("userToUserId")
  default UUID mapUserToUserId(User user) {
    return user == null ? null : user.getUserId();
  }

}
