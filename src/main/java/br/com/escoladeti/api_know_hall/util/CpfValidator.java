package br.com.escoladeti.api_know_hall.util;

import org.springframework.stereotype.Component;

@Component
public class CpfValidator {


  public boolean isValid(String cpf) {
    if (cpf == null || cpf.length() != 11 || !cpf.matches("\\d{11}")) {
      return false;
    }

    if (cpf.matches("(\\d)\\1{10}")) {
      return false;
    }

    int soma = 0;
    for (int i = 0; i < 9; i++) {
      soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
    }
    int primeiroDigito = 11 - (soma % 11);
    if (primeiroDigito >= 10) primeiroDigito = 0;

    soma = 0;
    for (int i = 0; i < 10; i++) {
      soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
    }
    int segundoDigito = 11 - (soma % 11);
    if (segundoDigito >= 10) segundoDigito = 0;

    return Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
      Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
  }

  public String formatarCpf(String cpf) {
    if (cpf == null) return null;
    return cpf.replaceAll("[^0-9]", "");
  }
}
