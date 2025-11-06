package br.com.escoladeti.api_know_hall.projection.comentario;

import java.math.BigInteger;
import java.sql.Timestamp;

public interface ComentarioProjection {
  BigInteger getId();
  BigInteger getPostId();
  BigInteger getUsuarioId();
  String getUsuarioNome();
  String getTexto();
  Long getTotalUpVotes();
  Long getTotalSuperVotes();
  BigInteger getComentarioPaiId();
  Timestamp getDataCriacao();
}
