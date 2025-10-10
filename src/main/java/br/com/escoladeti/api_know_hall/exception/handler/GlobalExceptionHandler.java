package br.com.escoladeti.api_know_hall.exception.handler;

import br.com.escoladeti.api_know_hall.dto.ErrorResponse;
import br.com.escoladeti.api_know_hall.exception.PalavraProibidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PalavraProibidaException.class)
  public ResponseEntity<ErrorResponse> handlePalavraProibida(PalavraProibidaException ex) {
    ErrorResponse error = new ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      ex.getMessage(),
      LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
    MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
      errors.put(error.getField(), error.getDefaultMessage())
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    ErrorResponse error = new ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      ex.getMessage(),
      LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    ErrorResponse error = new ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "Erro interno no servidor",
      LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
