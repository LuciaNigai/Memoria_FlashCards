package com.lucia.memoria.dto.local;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldDTO {

  @NotNull
  @Valid
  private TemplateFieldDTO templateField;
  private UUID id;
  private String content;

  public static FieldDTO blankWithTemplate(TemplateFieldDTO templateFieldDTO) {
    FieldDTO dto = new FieldDTO();
    dto.setContent(null);
    dto.setTemplateField(templateFieldDTO);
    return dto;
  }
}
