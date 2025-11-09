package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoListResponseDTO;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoRequestDTO;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.service.HistoricoTransacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/historico-transacoes")
@RequiredArgsConstructor
public class HistoricoTransacaoController {

  private final HistoricoTransacaoService historicoTransacaoService;

  @GetMapping
  public ResponseEntity<HistoricoTransacaoListResponseDTO> buscarHistorico(
    Principal principal,
    @RequestParam(required = false, defaultValue = "0") Integer page,
    @RequestParam(required = false, defaultValue = "20") Integer size,
    @RequestParam(required = false) MotivoTransacao motivo,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
  ) {
    Timestamp timestampInicio = dataInicio != null ? Timestamp.valueOf(dataInicio) : null;
    Timestamp timestampFim = dataFim != null ? Timestamp.valueOf(dataFim) : null;

    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(
      page,
      size,
      motivo,
      timestampInicio,
      timestampFim
    );

    HistoricoTransacaoListResponseDTO response = historicoTransacaoService
      .buscarHistoricoUsuario(principal, request);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/resumo")
  public ResponseEntity<ResumoTransacoesDTO> buscarResumo(Principal principal) {
    HistoricoTransacaoRequestDTO request = new HistoricoTransacaoRequestDTO(0, 1, null, null, null);

    HistoricoTransacaoListResponseDTO response = historicoTransacaoService
      .buscarHistoricoUsuario(principal, request);

    ResumoTransacoesDTO resumo = new ResumoTransacoesDTO(
      response.totalRecebido(),
      response.totalGasto(),
      response.saldoAtual(),
      response.totalElements()
    );

    return ResponseEntity.ok(resumo);
  }

  public record ResumoTransacoesDTO(
    Long totalRecebido,
    Long totalGasto,
    Long saldoAtual,
    Long totalTransacoes
  ) {}
}
