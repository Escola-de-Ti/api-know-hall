package br.com.escoladeti.api_know_hall.dto.post;

import br.com.escoladeti.api_know_hall.dto.ImagemPostDTO;
import br.com.escoladeti.api_know_hall.dto.tags.TagResponseDTO;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

public record PostFeedDTO(
  BigInteger id,
  BigInteger usuarioId,
  String nomeUsuario,
  String titulo,
  String descricao,
  Long totalUpVotes,
  List<TagResponseDTO> tags,
  Timestamp dataCriacao,
  Double relevanceScore,
  Integer tagsEmComum,
  Boolean jaVotou,
  List<ImagemPostDTO> imagens
) {
}
