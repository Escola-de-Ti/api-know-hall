package br.com.escoladeti.api_know_hall.dto.post;

import java.math.BigInteger;
import java.util.List;

public record PostBuscaResponseDTO(
  List<PostBuscaItemDTO> posts,
  Boolean hasMore,
  BigInteger lastPostId,
  Long lastValue
) {}
