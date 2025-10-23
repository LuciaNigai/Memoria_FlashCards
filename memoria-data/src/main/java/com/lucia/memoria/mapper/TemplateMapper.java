package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TemplateDTO;
import com.lucia.memoria.model.Template;
import com.lucia.memoria.model.User;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(uses = TemplateFieldMapper.class)
public interface TemplateMapper {

  @Mapping(target = "ownerId", source = "owner", qualifiedByName = "ownerToOwnerId")
  TemplateDTO toDTO(Template template);

  @Mapping(target = "ownerId", source = "owner", qualifiedByName = "ownerToOwnerId")
  List<TemplateDTO> toDTOList(List<Template> templateList);

  Template toEntity(TemplateDTO templateDTO);

  @Named("ownerToOwnerId")
  default UUID mapOwnerToOwnerId(User user) {
    return user == null ? null : user.getUserId();
  }
}
