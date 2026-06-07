package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TemplateFieldDTO;
import com.lucia.memoria.model.TemplateField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemplateFieldMapper {

  @Mapping(target = "id", source = "templateFieldId")
  TemplateFieldDTO toDTO(TemplateField templateField);

  @Mapping(target = "id", ignore = true)
  TemplateField toEntity(TemplateFieldDTO templateFieldDTO);
}
