package br.com.escoladeti.api_know_hall.dto.comentario;

import java.math.BigInteger;
import java.sql.Timestamp;

public record ComentarioResponseDTO(
  BigInteger id,
  BigInteger postId,
  BigInteger usuarioId,
  String usuarioNome,
  String texto,
  Long totalUpVotes,
  Long totalSuperVotes,
  BigInteger comentarioPaiId,
  Timestamp dataCriacao,
  Boolean jaVotou,
  Integer nivel
) {}
