package br.com.escoladeti.api_know_hall.exception;

public class TokenInsuficienteException extends RuntimeException {
  public TokenInsuficienteException(String message) {
    super(message);
  }
}
