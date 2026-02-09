package com.lucia.memoria.dto.local;

import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.helper.TemplateFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldDTO {

  private UUID templateFieldId;
  @NotBlank
  private String name;
  @NotNull
  private FieldRole fieldRole;
  private TemplateFieldType templateFieldType;
}
