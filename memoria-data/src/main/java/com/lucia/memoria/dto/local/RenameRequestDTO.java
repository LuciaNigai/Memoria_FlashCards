package com.lucia.memoria.dto.local;

import jakarta.validation.constraints.NotBlank;

public record RenameRequestDTO(@NotBlank String name) {

}
