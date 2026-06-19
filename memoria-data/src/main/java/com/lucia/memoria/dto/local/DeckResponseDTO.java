package com.lucia.memoria.dto.local;


import com.lucia.memoria.helper.AccessLevel;
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
public class DeckResponseDTO {

  private UUID id;
  private UUID userId;
  private String name;
  private AccessLevel accessLevel;
  private String path;
  private List<DeckResponseDTO> childDecks = new ArrayList<>();
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
