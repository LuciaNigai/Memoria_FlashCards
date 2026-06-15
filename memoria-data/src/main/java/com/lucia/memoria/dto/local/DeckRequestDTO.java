package com.lucia.memoria.dto.local;

import com.lucia.memoria.helper.AccessLevel;
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
public class DeckRequestDTO {

  private UUID id;
  @NotBlank
  private String name;
  private AccessLevel accessLevel;
  private String path;
  @NotNull
  private UUID userId;
}
