package br.com.escoladeti.api_know_hall.enums;

public enum ImagemTipo {
  WORKSHOP("workshop"),
  USUARIO("usuario"),
  POST("post");

  private final String tipo;

  ImagemTipo(String tipo) {
    this.tipo = tipo;
  }

  public String getTipo() {
    return tipo;
  }

}

