package br.com.escoladeti.api_know_hall.util;

import org.springframework.stereotype.Component;

@Component
public class TelefoneValidator {

  public boolean isValid(String telefone) {
    if (telefone == null || telefone.isEmpty()) {
      return true; // Telefone pode ser opcional
    }

    String apenasNumeros = telefone.replaceAll("[^0-9]", "");

    if (apenasNumeros.length() < 10 || apenasNumeros.length() > 11) {
      return false;
    }

    int ddd = Integer.parseInt(apenasNumeros.substring(0, 2));
    if (ddd < 11 || ddd > 99) {
      return false;
    }

    if (apenasNumeros.length() == 11) {
      char terceiroDigito = apenasNumeros.charAt(2);
      if (terceiroDigito != '9') {
        return false;
      }
    }

    return true;
  }


  public String formatarTelefone(String telefone) {
    if (telefone == null) return null;
    return telefone.replaceAll("[^0-9]", "");
  }

  public String formatarParaExibicao(String telefone) {
    if (telefone == null) return null;

    String apenasNumeros = formatarTelefone(telefone);

    if (apenasNumeros.length() == 11) {
      return String.format("(%s) %s-%s",
        apenasNumeros.substring(0, 2),
        apenasNumeros.substring(2, 7),
        apenasNumeros.substring(7));
    } else if (apenasNumeros.length() == 10) {
      // Fixo: (XX) XXXX-XXXX
      return String.format("(%s) %s-%s",
        apenasNumeros.substring(0, 2),
        apenasNumeros.substring(2, 6),
        apenasNumeros.substring(6));
    }

    return telefone;
  }
}
