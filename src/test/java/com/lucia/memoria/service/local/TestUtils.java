package com.lucia.memoria.service.local;


import com.lucia.memoria.dto.local.FieldDTO;
import com.lucia.memoria.dto.local.TemplateFieldDTO;
import com.lucia.memoria.model.Field;
import com.lucia.memoria.model.TemplateField;
import java.util.UUID;

public class TestUtils {

  public static TemplateField createTemplateField(UUID id) {
    TemplateField tf = new TemplateField();
    tf.setTemplateFieldId(id);
    return tf;
  }

  public static Field createField(TemplateField templateField) {
    Field field = new Field();
    field.setTemplateField(templateField);
    return field;
  }

  public static FieldDTO createFieldDTO(TemplateFieldDTO templateFieldDTO) {
    FieldDTO fieldDTO = new FieldDTO();
    fieldDTO.setFieldTemplate(templateFieldDTO);
    return fieldDTO;
  }

}
