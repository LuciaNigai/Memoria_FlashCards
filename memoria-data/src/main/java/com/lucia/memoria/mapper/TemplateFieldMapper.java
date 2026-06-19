package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.TemplateFieldRequestDTO;
import com.lucia.memoria.dto.local.TemplateFieldResponseDTO;
import com.lucia.memoria.model.TemplateField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TemplateFieldMapper {

  @Mapping(target = "id", source = "templateFieldId")
  TemplateFieldResponseDTO toDTO(TemplateField templateField);

  @Mapping(target = "id", ignore = true)
  TemplateField toEntity(TemplateFieldRequestDTO templateFieldResponseDTO);
}
