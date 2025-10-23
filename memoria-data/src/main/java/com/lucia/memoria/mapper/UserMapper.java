package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.UserDTO;
import com.lucia.memoria.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  User toEntity(UserDTO userDTO);

  UserDTO toDTO(User user);
}