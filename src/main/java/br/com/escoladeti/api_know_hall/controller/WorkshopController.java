package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopCreateDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopResponseDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
public class WorkshopController {

  private final WorkshopService workshopService;

  @PostMapping
  public ResponseEntity<WorkshopResponseDTO> criarWorkshop(
    @Valid @RequestBody WorkshopCreateDTO dto) {

    Workshop workshop = workshopService.criarWorkshop(dto);
    WorkshopResponseDTO response = WorkshopResponseDTO.fromEntity(workshop);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<WorkshopResponseDTO>> listarWorkshops(
    @RequestParam(required = false) StatusWorkshop status,
    @RequestParam(required = false) BigInteger instrutorId) {

    List<Workshop> workshops;

    if (status != null) {
      workshops = workshopService.listarPorStatus(status);
    } else if (instrutorId != null) {
      workshops = workshopService.listarPorInstrutor(instrutorId);
    } else {
      workshops = workshopService.listarTodos();
    }

    List<WorkshopResponseDTO> response = workshops.stream()
      .map(WorkshopResponseDTO::fromEntity)
      .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<WorkshopResponseDTO> buscarWorkshop(
    @PathVariable BigInteger id) {

    Workshop workshop = workshopService.buscarPorId(id);
    WorkshopResponseDTO response = WorkshopResponseDTO.fromEntity(workshop);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/abertos")
  public ResponseEntity<List<WorkshopResponseDTO>> listarWorkshopsAbertos() {
    List<Workshop> workshops = workshopService.listarWorkshopsAbertos();

    List<WorkshopResponseDTO> response = workshops.stream()
      .map(WorkshopResponseDTO::fromEntity)
      .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/buscar")
  public ResponseEntity<List<WorkshopResponseDTO>> buscarPorTitulo(
    @RequestParam String termo) {

    List<Workshop> workshops = workshopService.buscarPorTitulo(termo);

    List<WorkshopResponseDTO> response = workshops.stream()
      .map(WorkshopResponseDTO::fromEntity)
      .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/instrutor/{instrutorId}/count")
  public ResponseEntity<Long> contarWorkshopsPorInstrutor(
    @PathVariable BigInteger instrutorId) {

    Long count = workshopService.contarWorkshopsPorInstrutor(instrutorId);
    return ResponseEntity.ok(count);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<WorkshopResponseDTO> atualizarWorkshop(
    @PathVariable BigInteger id,
    @Valid @RequestBody WorkshopUpdateDTO dto) {

    Workshop workshop = workshopService.atualizarWorkshop(id, dto);
    WorkshopResponseDTO response = WorkshopResponseDTO.fromEntity(workshop);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarWorkshop(@PathVariable BigInteger id) {
    workshopService.deletarWorkshop(id);
    return ResponseEntity.noContent().build();
  }
}
