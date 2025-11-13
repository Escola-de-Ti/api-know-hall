package br.com.escoladeti.api_know_hall.dto.comentario;

import java.math.BigInteger;

public record ComentarioUsuarioResponseDTO(
  BigInteger comentarioId,
  BigInteger postId,
  String texto
) {
}

