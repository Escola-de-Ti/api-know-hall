package br.com.escoladeti.api_know_hall.util;

import org.springframework.stereotype.Component;

@Component
public class NomeValidator {

  private static final int MIN_LENGTH = 2;
  private static final int MAX_LENGTH = 100;

  public boolean isValid(String nome) {
    if (nome == null || nome.trim().isEmpty()) {
      return false;
    }

    String nomeTrimmed = nome.trim();

    if (nomeTrimmed.length() < MIN_LENGTH || nomeTrimmed.length() > MAX_LENGTH) {
      return false;
    }

    if (!nomeTrimmed.matches("^[a-zA-ZÀ-ÿ\\s'-]+$")) {
      return false;
    }

    if (!nomeTrimmed.matches(".*[a-zA-ZÀ-ÿ].*")) {
      return false;
    }

    if (nomeTrimmed.matches(".*\\s{2,}.*")) {
      return false;
    }

    return true;
  }

  public String normalizar(String nome) {
    if (nome == null) return null;

    String nomeTrimmed = nome.trim().replaceAll("\\s+", " ");
    String[] palavras = nomeTrimmed.split(" ");
    StringBuilder nomeNormalizado = new StringBuilder();

    for (String palavra : palavras) {
      if (!palavra.isEmpty()) {
        nomeNormalizado.append(Character.toUpperCase(palavra.charAt(0)))
          .append(palavra.substring(1).toLowerCase())
          .append(" ");
      }
    }

    return nomeNormalizado.toString().trim();
  }
}
