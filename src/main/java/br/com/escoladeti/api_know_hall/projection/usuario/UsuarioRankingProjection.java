package br.com.escoladeti.api_know_hall.projection.usuario;

public interface UsuarioRankingProjection {
  Long getPosicao();
  String getNome();
  Integer getQntdXp();
  Integer getNivel();
}
