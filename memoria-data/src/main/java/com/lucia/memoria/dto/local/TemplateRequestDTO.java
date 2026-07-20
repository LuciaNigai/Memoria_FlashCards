package com.lucia.memoria.dto.local;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequestDTO {
  @NotNull(message = "ownerId should be present")
  private UUID ownerId;
  @NotBlank(message = "name cannot be empty")
  private String name;
  @NotNull(message = "template fields list cannot be null")
  @Size(min = 2, message = "Template should have at least two fields")
  private List<TemplateFieldRequestDTO> fields;
}
