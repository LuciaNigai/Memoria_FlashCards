package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.FieldDTO;
import com.lucia.memoria.model.Field;
import org.mapstruct.Mapper;

@Mapper(uses = TemplateFieldMapper.class)
public interface FieldMapper {

  FieldDTO toDTO(Field field);
}
