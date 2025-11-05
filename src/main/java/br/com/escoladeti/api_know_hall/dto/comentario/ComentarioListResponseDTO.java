package br.com.escoladeti.api_know_hall.dto.comentario;

import java.math.BigInteger;
import java.util.List;

public record ComentarioListResponseDTO(
  List<ComentarioResponseDTO> comentarios,
  boolean hasMore,
  BigInteger lastComentarioId
) {}
