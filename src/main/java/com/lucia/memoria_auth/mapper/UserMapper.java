package com.lucia.memoria_auth.mapper;

import com.lucia.memoria_auth.dto.UserProfileDto;
import com.lucia.memoria_auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserProfileDto toUserProfileDto(final User user) {
    return new UserProfileDto(user.getEmail(), user.getUsername());
  }
}