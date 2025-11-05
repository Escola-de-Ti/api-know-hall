package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.voto.VotoResponseDTO;
import br.com.escoladeti.api_know_hall.service.VotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.Principal;

@RestController
@RequestMapping("/api/votos")
@RequiredArgsConstructor
public class VotoController {

  private final VotoService votoService;

  @PostMapping("/post/{postId}")
  public ResponseEntity<VotoResponseDTO> votarEmPost(
    @PathVariable BigInteger postId,
    Principal principal
  ) {
    VotoResponseDTO response = votoService.votarEmPost(postId, principal);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/comentario/{comentarioId}")
  public ResponseEntity<VotoResponseDTO> votarEmComentario(
    @PathVariable BigInteger comentarioId,
    Principal principal
  ) {
    VotoResponseDTO response = votoService.votarEmComentario(comentarioId, principal);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/comentario/{comentarioId}/super")
  public ResponseEntity<VotoResponseDTO> superVotarEmComentario(
    @PathVariable BigInteger comentarioId,
    Principal principal
  ) {
    VotoResponseDTO response = votoService.superVotarEmComentario(comentarioId, principal);
    return ResponseEntity.ok(response);
  }
}
