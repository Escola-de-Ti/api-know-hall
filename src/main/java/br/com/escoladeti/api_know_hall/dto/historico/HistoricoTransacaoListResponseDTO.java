package br.com.escoladeti.api_know_hall.dto.historico;

import java.util.List;

public record HistoricoTransacaoListResponseDTO(
  List<HistoricoTransacaoResponseDTO> transacoes,
  Long totalRecebido,
  Long totalGasto,
  Long saldoAtual,
  boolean hasMore,
  int totalPages,
  long totalElements
) {}
