package br.com.escoladeti.api_know_hall.dto.post;

import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioResponseDTO;
import br.com.escoladeti.api_know_hall.dto.tags.TagResponseDTO;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

public record PostDetalhesDTO(
  BigInteger id,
  BigInteger usuarioId,
  String usuarioNome,
  String titulo,
  String descricao,
  Long totalUpVotes,
  List<TagResponseDTO> tags,
  Timestamp dataCriacao,
  List<ComentarioResponseDTO> comentarios,
  boolean hasMoreComentarios,
  Boolean jaVotou
) {}
