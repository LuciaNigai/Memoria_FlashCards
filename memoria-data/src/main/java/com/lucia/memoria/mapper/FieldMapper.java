package com.lucia.memoria.mapper;

import com.lucia.memoria.dto.local.FieldDTO;
import com.lucia.memoria.dto.local.FieldMinimalDTO;
import com.lucia.memoria.model.Field;
import com.lucia.memoria.model.TemplateField;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(uses = TemplateFieldMapper.class)
public interface FieldMapper {

  FieldDTO toDTO(Field field);

  @Mapping(target = "templateFieldId", source = "templateField", qualifiedByName = "templateFieldToTemplateFieldId")
  FieldMinimalDTO toMinimalDTO(Field field);

  @Mapping(target = "templateFieldId", source = "templateField", qualifiedByName = "templateFieldToTemplateFieldId")
  List<FieldMinimalDTO> toMinimalDTOList(List<Field> fields);

  Field toEntity(FieldDTO fieldDTO);

  @Named("templateFieldToTemplateFieldId")
  default UUID mapTemplateFieldToTemplateFieldId(TemplateField templateField) {
    return templateField == null ? null : templateField.getTemplateFieldId();
  }
}
