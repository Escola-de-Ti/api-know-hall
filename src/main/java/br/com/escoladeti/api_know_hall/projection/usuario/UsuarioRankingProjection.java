package br.com.escoladeti.api_know_hall.projection.usuario;

import java.math.BigInteger;

public interface UsuarioRankingProjection {
  BigInteger getId();

  Long getPosicao();

  String getNome();

  Integer getQntdXp();

  Integer getNivel();
}
