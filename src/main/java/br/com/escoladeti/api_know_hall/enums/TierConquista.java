package br.com.escoladeti.api_know_hall.enums;

public enum TierConquista {
  BRONZE(1),
  PRATA(2),
  OURO(3),
  PLATINA(4),
  DIAMANTE(5);

  private final int nivel;

  TierConquista(int nivel) {
    this.nivel = nivel;
  }

  public int getNivel() {
    return nivel;
  }
}
