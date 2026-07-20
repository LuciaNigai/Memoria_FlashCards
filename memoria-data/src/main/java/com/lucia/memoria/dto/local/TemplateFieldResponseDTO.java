package com.lucia.memoria.dto.local;

import com.lucia.memoria.helper.FieldRole;
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
public class TemplateFieldResponseDTO {

  private UUID id;
  private String name;
  private FieldRole fieldRole;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
