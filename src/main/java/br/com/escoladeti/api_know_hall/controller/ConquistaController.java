package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.conquista.ConquistaCreateDTO;
import br.com.escoladeti.api_know_hall.dto.conquista.ConquistaResponseDTO;
import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.service.ConquistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conquistas")
@RequiredArgsConstructor
public class ConquistaController {

  private final ConquistaService conquistaService;

  @PostMapping
  public ResponseEntity<ConquistaResponseDTO> criarConquista(
    @Valid @RequestBody ConquistaCreateDTO dto) {

    Conquista conquista = conquistaService.criarConquistaComTiers(
      dto.getNome(),
      dto.getDescricao(),
      dto.getCampoValidacao(),
      dto.getTipoConquista(),
      dto.getTiers()
    );

    ConquistaResponseDTO response = ConquistaResponseDTO.fromEntity(conquista);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }


  @GetMapping
  public ResponseEntity<List<ConquistaResponseDTO>> listarConquistas(
    @RequestParam(required = false) TipoConquista tipo) {

    List<Conquista> conquistas;
    if (tipo != null) {
      conquistas = conquistaService.listarConquistasPorTipo(tipo);
    } else {
      conquistas = conquistaService.listarTodasConquistas();
    }

    List<ConquistaResponseDTO> response = conquistas.stream()
      .map(ConquistaResponseDTO::fromEntity)
      .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ConquistaResponseDTO> buscarConquista(
    @PathVariable BigInteger id) {

    Conquista conquista = conquistaService.buscarConquistaPorId(id);
    ConquistaResponseDTO response = ConquistaResponseDTO.fromEntity(conquista);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/campo/{campoValidacao}")
  public ResponseEntity<List<ConquistaResponseDTO>> listarPorCampo(
    @PathVariable String campoValidacao) {

    List<Conquista> conquistas = conquistaService.listarConquistasPorCampo(campoValidacao);

    List<ConquistaResponseDTO> response = conquistas.stream()
      .map(ConquistaResponseDTO::fromEntity)
      .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }
}

