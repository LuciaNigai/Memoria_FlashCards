package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.UserDTO;
import com.lucia.memoria.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  User toEntity(UserDTO userDTO);

  @Mapping(target = "id", source = "userId")
  UserDTO toDTO(User user);
}