package br.com.escoladeti.api_know_hall.dto.historico;

import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;

import java.math.BigInteger;
import java.sql.Timestamp;

public record HistoricoTransacaoResponseDTO(
  BigInteger id,
  Long quantidade,
  MotivoTransacao motivo,
  String motivoDescricao,
  String descricao,
  Timestamp dataTransacao
) {}
