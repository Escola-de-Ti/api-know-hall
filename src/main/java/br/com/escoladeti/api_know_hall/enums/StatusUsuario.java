package br.com.escoladeti.api_know_hall.enums;

public enum StatusUsuario {
  ATIVO("ativo"),
  INATIVO("inativo"),
  DELETADO("deletado"),
  CONFIRMACAO_PENDENTE("confirmacao_pendente");

  private final String statusUsuario;

  StatusUsuario(String status) {
    this.statusUsuario = status;
  }

  public String getStatusUsuario() {
    return statusUsuario;
  }
}

