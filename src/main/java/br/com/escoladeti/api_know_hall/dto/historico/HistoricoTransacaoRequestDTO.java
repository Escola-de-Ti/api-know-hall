package br.com.escoladeti.api_know_hall.dto.historico;

import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;

import java.sql.Timestamp;

public record HistoricoTransacaoRequestDTO(
  Integer page,
  Integer size,
  MotivoTransacao motivo,
  Timestamp dataInicio,
  Timestamp dataFim
) {
  public HistoricoTransacaoRequestDTO {
    if (page == null || page < 0) {
      page = 0;
    }

    if (size == null || size < 1) {
      size = 20;
    }
    if (size > 100) {
      size = 100;
    }
  }
}
