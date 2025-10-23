package com.lucia.memoria.exception;

import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException {
  private final Object duplicates;

  public DuplicateException(String message, Object duplicates) {
    super(message);
    this.duplicates = duplicates;
  }

}

