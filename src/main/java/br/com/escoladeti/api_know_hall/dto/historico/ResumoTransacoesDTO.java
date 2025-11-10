package br.com.escoladeti.api_know_hall.dto.historico;

public record ResumoTransacoesDTO(
  Long totalRecebido,
  Long totalGasto,
  Long saldoAtual,
  Long totalTransacoes
) {}
