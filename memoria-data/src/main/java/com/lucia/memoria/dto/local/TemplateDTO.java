package com.lucia.memoria.dto.local;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDTO {

  private UUID templateId;
  @NotNull
  private UUID ownerId;
  @NotBlank
  private String name;
  @NotNull
  @Size(min = 2, message = "Template should have at least two fields")
  @Valid
  private List<TemplateFieldDTO> fields;
  private Boolean includesPartOfSpeech;
}
