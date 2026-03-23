package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TagDTO;
import com.lucia.memoria.model.Tag;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {

  @Mapping(target = "userId", source = "user.userId")
  TagDTO toDTO(Tag tag);

  List<TagDTO> toDTOList(List<Tag> tags);
}
