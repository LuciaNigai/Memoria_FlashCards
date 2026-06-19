package com.lucia.memoria.dto.local;

import java.time.OffsetDateTime;
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
public class TemplateResponseDTO {

  private UUID id;
  private UUID ownerId;
  private String name;
  private List<TemplateFieldResponseDTO> fields;
  private boolean includesPartOfSpeech;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}