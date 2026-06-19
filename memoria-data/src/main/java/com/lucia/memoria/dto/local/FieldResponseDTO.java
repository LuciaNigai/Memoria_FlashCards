package com.lucia.memoria.dto.local;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldResponseDTO {

  @NotNull
  @Valid
  private TemplateFieldResponseDTO templateField;
  private UUID id;
  private String content;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public static FieldResponseDTO blankWithTemplate(
      TemplateFieldResponseDTO templateFieldResponseDTO) {
    FieldResponseDTO dto = new FieldResponseDTO();
    dto.setContent(null);
    dto.setTemplateField(templateFieldResponseDTO);
    return dto;
  }
}
