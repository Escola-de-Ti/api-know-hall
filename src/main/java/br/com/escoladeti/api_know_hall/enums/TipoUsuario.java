package br.com.escoladeti.api_know_hall.enums;

public enum TipoUsuario {
  ALUNO("aluno"),
  INSTRUTOR("instrutor"),
  ADMINISTRADOR("administrador");

  private final String tipoUsuario;

  TipoUsuario(String tipo) {
    this.tipoUsuario = tipo;
  }

  public String getTipoUsuario() {
    return tipoUsuario;
  }

}
