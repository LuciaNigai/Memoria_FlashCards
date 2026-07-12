package com.lucia.memoria.dto.local;

import com.lucia.memoria.helper.FieldRole;
import com.lucia.memoria.helper.TemplateFieldType;
import jakarta.validation.constraints.NotBlank;
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
public class TemplateFieldRequestDTO {

  private UUID id;
  @NotBlank(message = "name cannot be empty")
  private String name;
  @NotNull(message = "fieldRole should be present")
  private FieldRole fieldRole;
  private TemplateFieldType templateFieldType;
}
