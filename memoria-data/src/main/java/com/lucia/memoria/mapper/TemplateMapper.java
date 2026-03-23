package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TemplateDTO;
import com.lucia.memoria.model.Template;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TemplateFieldMapper.class)
public interface TemplateMapper {

  @Mapping(target = "ownerId", source = "owner.userId")
  TemplateDTO toDTO(Template template);

  List<TemplateDTO> toDTOList(List<Template> templateList);
}
