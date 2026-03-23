package com.lucia.memoria.exception;

import com.lucia.memoria.dto.local.DuplicateErrorResponseDTO;
import com.lucia.memoria.dto.local.GeneralResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
    return ResponseEntity
        .badRequest()
        .body(ex.getMessage());
  }

  @ExceptionHandler(DuplicateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<DuplicateErrorResponseDTO> handleDuplicateException(DuplicateException ex) {
    DuplicateErrorResponseDTO response = new DuplicateErrorResponseDTO(
        ex.getMessage(),
        ex.getDuplicates()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(ConflictWithDataException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<GeneralResponseDTO<?>> handleConflictWithDataException(
      ConflictWithDataException ex) {
    GeneralResponseDTO<?> response = new GeneralResponseDTO<>(
        ex.getMessage(),
        ex.getData()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<GeneralResponseDTO<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message = String.format(
        "Invalid value for parameter '%s'",
        ex.getName()
    );
    return ResponseEntity.badRequest().body(new GeneralResponseDTO<>(message, ex.getValue()));
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<GeneralResponseDTO<?>> handleNotFoundException(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GeneralResponseDTO<>(ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<GeneralResponseDTO<?>> handleGeneralException(
      IllegalArgumentException ex) {
    GeneralResponseDTO<?> response = new GeneralResponseDTO<>(
        ex.getMessage(),
        HttpStatus.BAD_REQUEST
    );
    return ResponseEntity.badRequest().body(response);
  }
}
