package com.lucia.memoria.dto.local;

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
public class CardRequestDTO {

  private UUID id;
  @NotNull(message = "deckId should be present")
  private UUID deckId;
  @NotNull(message = "templateId should be present")
  private UUID templateId;
  @NotNull(message = "fields list cannot be null")
  @Size(min = 2, message = "Card should have at least two fields")
  private List<FieldRequestDTO> fields;
}
