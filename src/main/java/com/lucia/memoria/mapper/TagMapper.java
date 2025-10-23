package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TagDTO;
import com.lucia.memoria.model.Tag;
import com.lucia.memoria.model.User;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TagMapper {

  Tag toEntity(TagDTO tagDTO);

  @Mapping(target = "userId", source = "user", qualifiedByName = "userToUserId")
  TagDTO toDTO(Tag tag);

  @Mapping(target = "userId", source = "user", qualifiedByName = "userToUserId")
  List<TagDTO> toDTOList(List<Tag> tags);

  @Named("userToUserId")
  default UUID userToUserId(User user) {
    return user == null ? null : user.getUserId();
  }
}
