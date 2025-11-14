package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.comentario.*;
import br.com.escoladeti.api_know_hall.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

  private final ComentarioService comentarioService;

  @PostMapping
  public ResponseEntity<ComentarioResponseDTO> criarComentario(
    @Valid @RequestBody ComentarioCreateDTO dto,
    Principal principal
  ) {
    ComentarioResponseDTO comentario = comentarioService.criarComentario(dto, principal);
    return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
  }

  @GetMapping("/post/{postId}")
  public ResponseEntity<ComentarioListResponseDTO> buscarComentariosDoPost(
    @PathVariable BigInteger postId,
    @RequestParam(required = false) BigInteger lastComentarioId,
    @RequestParam(required = false, defaultValue = "20") Integer pageSize,
    Principal principal
  ) {
    ComentarioListResponseDTO comentarios = comentarioService.buscarComentariosDoPost(
      postId,
      lastComentarioId,
      pageSize,
      principal.getName()
    );
    return ResponseEntity.ok(comentarios);
  }

  @GetMapping("/{comentarioPaiId}/respostas")
  public ResponseEntity<ComentarioListResponseDTO> buscarRespostasDoComentario(
    @PathVariable BigInteger comentarioPaiId,
    @RequestParam(required = false) BigInteger lastComentarioId,
    @RequestParam(required = false, defaultValue = "10") Integer pageSize,
    Principal principal
  ) {
    ComentarioListResponseDTO respostas = comentarioService.buscarRespostasDoComentario(
      comentarioPaiId,
      lastComentarioId,
      pageSize,
      principal.getName()
    );
    return ResponseEntity.ok(respostas);
  }

  @GetMapping("/meus")
  public ResponseEntity<ComentarioListResponseDTO> buscarMeusComentarios(
    @RequestParam(required = false) BigInteger lastComentarioId,
    @RequestParam(required = false, defaultValue = "20") Integer pageSize,
    Principal principal
  ) {
    ComentarioListResponseDTO comentarios = comentarioService.buscarComentariosDoUsuario(
      principal,
      lastComentarioId,
      pageSize
    );
    return ResponseEntity.ok(comentarios);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ComentarioResponseDTO> atualizarComentario(
    @PathVariable BigInteger id,
    @Valid @RequestBody ComentarioUpdateDTO dto,
    Principal principal
  ) {
    ComentarioResponseDTO comentario = comentarioService.atualizarComentario(id, dto, principal);
    return ResponseEntity.ok(comentario);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarComentario(
    @PathVariable BigInteger id,
    Principal principal
  ) {
    comentarioService.deletarComentario(id, principal);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/usuario/{usuarioId}")
  public ResponseEntity<List<ComentarioUsuarioResponseDTO>> buscarTodosComentariosDoUsuario(
    @PathVariable BigInteger usuarioId
  ) {
    List<ComentarioUsuarioResponseDTO> comentarios =
      comentarioService.buscarTodosComentariosDoUsuario(usuarioId);
    return ResponseEntity.ok(comentarios);
  }
}
