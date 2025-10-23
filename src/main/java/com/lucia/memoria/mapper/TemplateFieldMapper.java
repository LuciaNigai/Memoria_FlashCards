package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TemplateFieldDTO;
import com.lucia.memoria.model.TemplateField;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TemplateFieldMapper {

  TemplateFieldDTO toDTO(TemplateField templateField);

  TemplateField toEntity(TemplateFieldDTO templateFieldDTO);
}
