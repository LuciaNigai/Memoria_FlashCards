package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TagResponseDTO;
import com.lucia.memoria.model.Tag;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TagMapper {

  @Mapping(target = "id", source = "tagId")
  TagResponseDTO toDTO(Tag tag);

  List<TagResponseDTO> toDTOList(List<Tag> tags);
}
