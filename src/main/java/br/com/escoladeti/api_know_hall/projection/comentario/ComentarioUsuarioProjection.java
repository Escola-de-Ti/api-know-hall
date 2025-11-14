package br.com.escoladeti.api_know_hall.projection.comentario;

import java.math.BigInteger;
import java.util.Date;

public interface ComentarioUsuarioProjection {
  BigInteger getComentarioId();

  BigInteger getPostId();

  String getTexto();

  Date getDataCriacao();
}

