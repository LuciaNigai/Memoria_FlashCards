package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.FieldRequestDTO;
import com.lucia.memoria.dto.local.FieldResponseDTO;
import com.lucia.memoria.model.Field;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TemplateFieldMapper.class)
public interface FieldMapper {

  @Mapping(target = "id", source = "fieldId")
  FieldResponseDTO toDTO(Field field);

  @Mapping(target = "templateFieldId", source = "templateField.templateFieldId")
  FieldRequestDTO toMinimalDTO(Field field);
}
