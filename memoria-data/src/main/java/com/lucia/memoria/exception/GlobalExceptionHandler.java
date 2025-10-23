package com.lucia.memoria.exception;

import com.lucia.memoria.dto.local.DuplicateErrorResponseDTO;
import com.lucia.memoria.dto.local.GeneralResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
    return ResponseEntity
        .badRequest()
        .body(ex.getMessage());
  }

//  Used when user tries to save a resource, but it already exists
  @ExceptionHandler(DuplicateException.class)
  public ResponseEntity<DuplicateErrorResponseDTO> handleDuplicateException(DuplicateException ex) {
    DuplicateErrorResponseDTO response = new DuplicateErrorResponseDTO(
        ex.getMessage(),
        ex.getDuplicates()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

//  Used mostly when user tries to delete a resource, but it has related child data
  @ExceptionHandler(ConflictWithDataException.class)
  public ResponseEntity<GeneralResponseDTO<?>> handleConflictWithDataException(
      ConflictWithDataException ex) {
    GeneralResponseDTO<?> response = new GeneralResponseDTO<>(
        ex.getMessage(),
        ex.getData()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<GeneralResponseDTO<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message = String.format(
        "Invalid value for parameter '%s'",
        ex.getName()
    );
    return ResponseEntity.badRequest().body(new GeneralResponseDTO<>(message, ex.getValue()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<GeneralResponseDTO<?>> handleGeneralException(
      IllegalArgumentException ex) {
    GeneralResponseDTO<?> response = new GeneralResponseDTO<>(
        ex.getMessage(),
        HttpStatus.BAD_REQUEST
    );
    return ResponseEntity.badRequest().body(response);
  }
}
