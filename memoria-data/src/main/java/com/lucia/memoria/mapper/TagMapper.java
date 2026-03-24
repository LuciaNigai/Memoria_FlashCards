package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TagDTO;
import com.lucia.memoria.model.Tag;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

  TagDTO toDTO(Tag tag);

  List<TagDTO> toDTOList(List<Tag> tags);
}
