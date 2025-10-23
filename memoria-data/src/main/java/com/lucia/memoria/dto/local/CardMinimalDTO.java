package com.lucia.memoria.dto.local;

import jakarta.validation.constraints.NotEmpty;
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
public class CardMinimalDTO {

  private UUID cardId;
  @NotNull
  private UUID deckId;
  @NotNull
  UUID templateId;
  @NotNull
  @Size(min = 2, message = "Card should have at least two fields")
  List<FieldMinimalDTO> fieldMinimalDTOList;
}
