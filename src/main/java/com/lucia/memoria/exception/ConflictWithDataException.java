package com.lucia.memoria.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class ConflictWithDataException extends RuntimeException {
  private Object data;

  public ConflictWithDataException(String message, Object data) {
    super(message);
    this.data = data;
  }
  public ConflictWithDataException(String message) {
    super(message);
  }

}
