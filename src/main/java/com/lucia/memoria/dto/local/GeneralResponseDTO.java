package com.lucia.memoria.dto.local;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GeneralResponseDTO<T> {
  private String message;
  private T data;

  public GeneralResponseDTO(String message) {
    this.message=message;
  }

  public GeneralResponseDTO(T data) {
    this.data=data;
  }
}
