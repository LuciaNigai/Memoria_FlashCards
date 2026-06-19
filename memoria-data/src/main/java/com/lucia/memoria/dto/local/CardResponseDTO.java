package com.lucia.memoria.dto.local;


import java.time.OffsetDateTime;
import java.util.ArrayList;
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
public class CardResponseDTO {

  private UUID id;
  private UUID deckId;
  private UUID templateId;
  private List<FieldResponseDTO> fields = new ArrayList<>();
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
