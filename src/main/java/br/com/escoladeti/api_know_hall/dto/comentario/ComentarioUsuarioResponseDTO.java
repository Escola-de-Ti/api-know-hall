package br.com.escoladeti.api_know_hall.dto.comentario;

import java.math.BigInteger;
import java.util.Date;

public record ComentarioUsuarioResponseDTO(
  BigInteger comentarioId,
  BigInteger postId,
  String texto,
  Date dataCriacao
) {
}

