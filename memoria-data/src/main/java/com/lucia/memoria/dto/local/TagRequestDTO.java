package com.lucia.memoria.dto.local;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record TagRequestDTO(UUID id, @NotBlank String name, @NotBlank String color) {
}
