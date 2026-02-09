package com.lucia.memoria.dto.local;

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
public class CardRequestDTO {

  private UUID cardId;
  @NotNull
  private UUID deckId;
  @NotNull
  private UUID templateId;
  @NotNull
  @Size(min = 2, message = "Card should have at least two fields")
  private List<FieldMinimalDTO> fieldRequestDTOList;
}
