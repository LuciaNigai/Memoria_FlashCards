package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TemplateResponseDTO;
import com.lucia.memoria.model.Template;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TemplateFieldMapper.class)
public interface TemplateMapper {

  @Mapping(target = "ownerId", source = "owner.userId")
  @Mapping(target = "id", source = "templateId")
  TemplateResponseDTO toDTO(Template template);

  List<TemplateResponseDTO> toDTOList(List<Template> templateList);
}
