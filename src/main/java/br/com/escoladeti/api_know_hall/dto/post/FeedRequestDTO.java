package br.com.escoladeti.api_know_hall.dto.post;

import br.com.escoladeti.api_know_hall.enums.OrderBy;
import br.com.escoladeti.api_know_hall.enums.TagOperador;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

public record FeedRequestDTO(
  BigInteger usuarioId,
  Integer pageSize,
  BigInteger lastPostId,
  Double lastScore,
  List<BigInteger> tagIds,
  TagOperador tagOperador,
  LocalDate dataInicio,
  LocalDate dataFim,
  OrderBy orderBy
) {
  public FeedRequestDTO {
    if (pageSize == null || pageSize <= 0) {
      pageSize = 20;
    }
    if (pageSize > 100) {
      pageSize = 100;
    }
    if (tagOperador == null) {
      tagOperador = TagOperador.OR;
    }
  }
}
