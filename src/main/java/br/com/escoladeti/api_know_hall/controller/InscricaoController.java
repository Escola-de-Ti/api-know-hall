package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.inscricao.InscricaoResponseDTO;
import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import br.com.escoladeti.api_know_hall.service.InscricaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inscricoes")
@RequiredArgsConstructor
public class InscricaoController {

  private final InscricaoService inscricaoService;

  @PostMapping
  public ResponseEntity<InscricaoResponseDTO> inscrever(
    @Valid @RequestBody BigInteger workshopId,
    Principal principal
  ) {
    InscricaoResponseDTO inscricao = inscricaoService.inscrever(principal.getName(), workshopId);
    return ResponseEntity.status(HttpStatus.CREATED).body(inscricao);
  }

  @DeleteMapping("/workshops/{workshopId}")
  public ResponseEntity<Void> cancelarInscricao(
    @PathVariable BigInteger workshopId,
    Principal principal
  ) {
    inscricaoService.cancelarInscricao(principal.getName(), workshopId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/workshops/{workshopId}")
  public ResponseEntity<InscricaoResponseDTO> buscarInscricao(
    @PathVariable BigInteger workshopId,
    Principal principal
  ) {
    InscricaoResponseDTO inscricao = inscricaoService.buscarInscricao(principal.getName(), workshopId);
    return ResponseEntity.ok(inscricao);
  }

  @GetMapping("/minhas")
  public ResponseEntity<List<InscricaoResponseDTO>> listarMinhasInscricoes(Principal principal) {
    List<InscricaoResponseDTO> inscricoes = inscricaoService.listarInscricoesPorUsuario(principal.getName());
    return ResponseEntity.ok(inscricoes);
  }

  @GetMapping("/workshops/{workshopId}/participantes")
  public ResponseEntity<List<InscricaoResponseDTO>> listarInscricoesPorWorkshop(
    @PathVariable BigInteger workshopId
  ) {
    List<InscricaoResponseDTO> inscricoes = inscricaoService.listarInscricoesPorWorkshop(workshopId);
    return ResponseEntity.ok(inscricoes);
  }

  @PatchMapping("/{inscricaoId}")
  public ResponseEntity<InscricaoResponseDTO> atualizarStatus(
    @PathVariable BigInteger inscricaoId,
    @Valid @RequestBody StatusInscricao novoStatus
  ) {
    InscricaoResponseDTO inscricao = inscricaoService.atualizarStatusInscricao(inscricaoId, novoStatus);
    return ResponseEntity.ok(inscricao);
  }
}
