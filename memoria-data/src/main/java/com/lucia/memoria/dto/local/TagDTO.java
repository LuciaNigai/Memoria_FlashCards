package com.lucia.memoria.dto.local;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record TagDTO(UUID userId, UUID tagId, @NotBlank String name) {

}
