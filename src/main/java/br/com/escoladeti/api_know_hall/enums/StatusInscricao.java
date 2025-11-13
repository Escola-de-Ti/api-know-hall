package br.com.escoladeti.api_know_hall.enums;

public enum StatusInscricao {
  INSCRITO("inscrito"),
  CANCELADO("cancelado"),
  EXPIRADO("expirado"),
  EM_BREVE("em_breve"),
  INATIVO("inativo");

  private final String statusInscricao;

  StatusInscricao(String status) {
    this.statusInscricao = status;
  }

  public String getStatusInscricao() {
    return statusInscricao;
  }
}
