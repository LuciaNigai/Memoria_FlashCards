package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.FieldDTO;
import com.lucia.memoria.dto.local.FieldMinimalDTO;
import com.lucia.memoria.model.Field;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TemplateFieldMapper.class)
public interface FieldMapper {

  FieldDTO toDTO(Field field);

  @Mapping(target = "templateFieldId", source = "templateField.templateFieldId")
  FieldMinimalDTO toMinimalDTO(Field field);
}
