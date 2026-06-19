package com.lucia.memoria.dto.local;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TagResponseDTO(UUID id, @NotBlank String name, @NotBlank String color,  OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
