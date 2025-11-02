package br.com.escoladeti.api_know_hall.dto.post;

import br.com.escoladeti.api_know_hall.enums.OrdenacaoDirecao;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoTipo;
import br.com.escoladeti.api_know_hall.enums.TagOperador;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

public record PostBuscaRequestDTO(
  List<BigInteger> tagIds,
  TagOperador tagOperador,
  LocalDate dataInicio,
  LocalDate dataFim,
  OrdenacaoTipo ordenacao,
  OrdenacaoDirecao direcao,
  Integer pageSize,
  BigInteger lastPostId,
  Long lastValue,
  String termo  // ✅ NOVO CAMPO
) {
  public PostBuscaRequestDTO {
    if (tagOperador == null) {
      tagOperador = TagOperador.OR;
    }
    if (ordenacao == null) {
      ordenacao = OrdenacaoTipo.DATA;
    }
    if (direcao == null) {
      direcao = OrdenacaoDirecao.DESC;
    }
    if (pageSize == null || pageSize <= 0) {
      pageSize = 20;
    }
    if (pageSize > 100) {
      pageSize = 100;
    }
  }
}
