package br.com.escoladeti.api_know_hall.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SenhaValidator {

  private static final int MIN_LENGTH = 8;
  private static final String CARACTERES_ESPECIAIS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

  /**
   * Valida se a senha atende aos requisitos de segurança
   * @param senha Senha a ser validada
   * @return true se a senha é válida
   */
  public boolean isValid(String senha) {
    if (senha == null || senha.isEmpty()) {
      return false;
    }

    // Verifica tamanho mínimo
    if (senha.length() < MIN_LENGTH) {
      return false;
    }

    // Verifica se contém pelo menos um número
    if (!senha.matches(".*\\d.*")) {
      return false;
    }

    // Verifica se contém pelo menos um caractere especial
    if (!contemCaractereEspecial(senha)) {
      return false;
    }

    return true;
  }

  /**
   * Verifica se a senha contém pelo menos um caractere especial
   */
  private boolean contemCaractereEspecial(String senha) {
    for (char c : senha.toCharArray()) {
      if (CARACTERES_ESPECIAIS.indexOf(c) >= 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retorna uma lista de erros de validação da senha
   * @param senha Senha a ser validada
   * @return Lista de mensagens de erro
   */
  public List<String> getErrosValidacao(String senha) {
    List<String> erros = new ArrayList<>();

    if (senha == null || senha.isEmpty()) {
      erros.add("Senha é obrigatória");
      return erros;
    }

    if (senha.length() < MIN_LENGTH) {
      erros.add("Senha deve ter no mínimo " + MIN_LENGTH + " caracteres");
    }

    if (!senha.matches(".*\\d.*")) {
      erros.add("Senha deve conter pelo menos um número");
    }

    if (!contemCaractereEspecial(senha)) {
      erros.add("Senha deve conter pelo menos um caractere especial (!@#$%^&*()_+-=[]{}|;:,.<>?)");
    }

    return erros;
  }

  /**
   * Retorna uma mensagem de erro formatada
   * @param senha Senha a ser validada
   * @return Mensagem de erro ou null se válida
   */
  public String getMensagemErro(String senha) {
    List<String> erros = getErrosValidacao(senha);
    if (erros.isEmpty()) {
      return null;
    }
    return String.join("; ", erros);
  }

  /**
   * Calcula a força da senha (0-4)
   * @param senha Senha a ser avaliada
   * @return Nível de força (0=muito fraca, 4=muito forte)
   */
  public int calcularForcaSenha(String senha) {
    if (senha == null || senha.isEmpty()) {
      return 0;
    }

    int forca = 0;

    if (senha.length() >= 8) forca++;
    if (senha.length() >= 12) forca++;

    if (senha.matches(".*\\d.*")) forca++;
    if (senha.matches(".*[a-z].*")) forca++;
    if (senha.matches(".*[A-Z].*")) forca++;
    if (contemCaractereEspecial(senha)) forca++;

    return Math.min(forca, 4);
  }


  public String getDescricaoForca(String senha) {
    int forca = calcularForcaSenha(senha);
    switch (forca) {
      case 0:
      case 1:
        return "Muito fraca";
      case 2:
        return "Fraca";
      case 3:
        return "Média";
      case 4:
        return "Forte";
      default:
        return "Desconhecida";
    }
  }
}
