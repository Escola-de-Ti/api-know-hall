package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.conquista.*;
import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.service.ConquistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios/conquistas")
@RequiredArgsConstructor
public class UsuarioConquistaController {

  private final ConquistaService conquistaService;

  @GetMapping
  public ResponseEntity<List<UsuarioConquistaResponseDTO>> listarConquistasUsuario(
    Principal principal,
    @RequestParam(required = false) TipoConquista tipo) {

    List<UsuarioConquista> conquistas;
    if (tipo != null) {
      conquistas = conquistaService.listarConquistasUsuarioPorTipo(principal.getName(), tipo);
    } else {
      conquistas = conquistaService.listarConquistasUsuario(principal.getName());
    }

    List<UsuarioConquistaResponseDTO> response = conquistas.stream()
      .map(UsuarioConquistaResponseDTO::fromEntity)
      .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{conquistaId}/progresso")
  public ResponseEntity<ConquistaProgressoResponseDTO> obterProgresso(
    Principal principal,
    @PathVariable BigInteger conquistaId) {

    ConquistaProgressoDTO progresso =
      conquistaService.obterProgressoConquista(principal.getName(), conquistaId);

    ConquistaProgressoResponseDTO response =
      ConquistaProgressoResponseDTO.fromDTO(progresso);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/verificar")
  public ResponseEntity<Map<String, Object>> verificarEConcederConquistas(
    Principal principal,
    @Valid @RequestBody VerificarProgressoDTO dto) {

    conquistaService.verificarEConcederConquistas(
      principal.getName(),
      dto.getCampoValidacao(),
      dto.getProgressoAtual()
    );

    Map<String, Object> response = new HashMap<>();
    response.put("mensagem", "Progresso verificado com sucesso");
    response.put("usuarioEmail", principal.getName());
    response.put("campoValidacao", dto.getCampoValidacao());
    response.put("progressoAtual", dto.getProgressoAtual());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/estatisticas")
  public ResponseEntity<Map<String, Object>> obterEstatisticas(
    Principal principal) {

    List<UsuarioConquista> todasConquistas =
      conquistaService.listarConquistasUsuario(principal.getName());

    long totalConquistas = todasConquistas.size();
    long totalInsignias = todasConquistas.stream()
      .filter(uc -> uc.getConquista().getTipoConquista() == TipoConquista.INSIGNIA)
      .count();
    long totalCertificados = todasConquistas.stream()
      .filter(uc -> uc.getConquista().getTipoConquista() == TipoConquista.CERTIFICADO)
      .count();

    Map<TierConquista, Long> conquistasPorTier = todasConquistas.stream()
      .collect(Collectors.groupingBy(
        uc -> uc.getConquistaTier().getTier(),
        Collectors.counting()
      ));

    Map<String, Object> response = new HashMap<>();
    response.put("totalConquistas", totalConquistas);
    response.put("totalInsignias", totalInsignias);
    response.put("totalCertificados", totalCertificados);
    response.put("conquistasPorTier", conquistasPorTier);

    return ResponseEntity.ok(response);
  }
}
