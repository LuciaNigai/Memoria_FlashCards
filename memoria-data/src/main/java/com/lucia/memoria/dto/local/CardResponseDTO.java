package com.lucia.memoria.dto.local;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CardResponseDTO {

  private UUID cardId;
  @NotNull
  private UUID deckId;
  @NotNull
  private UUID templateId;
  @NotEmpty
  private List<FieldDTO> fieldDTOList;
}
