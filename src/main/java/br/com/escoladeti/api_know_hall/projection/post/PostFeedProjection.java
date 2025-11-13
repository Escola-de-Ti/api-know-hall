package br.com.escoladeti.api_know_hall.projection.post;

import java.math.BigInteger;
import java.sql.Timestamp;

public interface PostFeedProjection {
  BigInteger getId();
  BigInteger getUsuarioId();
  String getUsuarioNome();
  String getTitulo();
  String getDescricao();
  Long getTotalUpVotes();
  Timestamp getDataCriacao();
  Double getRelevanceScore();
  Integer getTagsEmComum();
  Boolean getJaVotou();
}
