package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.post.*;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoDirecao;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoTipo;
import br.com.escoladeti.api_know_hall.enums.TagOperador;
import br.com.escoladeti.api_know_hall.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @PostMapping
  public ResponseEntity<PostResponseDTO> criarPost(@Valid @RequestBody PostCreateDTO dto) {
    PostResponseDTO post = postService.criarPost(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(post);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PostResponseDTO> buscarPost(@PathVariable BigInteger id) {
    PostResponseDTO post = postService.buscarPorId(id);
    return ResponseEntity.ok(post);
  }

  @GetMapping
  public ResponseEntity<List<PostResponseDTO>> listarTodos() {
    List<PostResponseDTO> posts = postService.listarTodos();
    return ResponseEntity.ok(posts);
  }

  @GetMapping("/usuario/{usuarioId}")
  public ResponseEntity<List<PostResponseDTO>> listarPorUsuario(@PathVariable BigInteger usuarioId) {
    List<PostResponseDTO> posts = postService.listarPorUsuario(usuarioId);
    return ResponseEntity.ok(posts);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<PostResponseDTO> atualizarPost(
    @PathVariable BigInteger id,
    @RequestBody PostUpdateDTO dto
  ) {
    PostResponseDTO post = postService.atualizarPost(id, dto);
    return ResponseEntity.ok(post);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarPost(@PathVariable BigInteger id) {
    postService.deletarPost(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/feed")
  public ResponseEntity<FeedResponseDTO> getFeed(
    @RequestParam BigInteger usuarioId,
    @RequestParam(required = false, defaultValue = "20") Integer pageSize,
    @RequestParam(required = false) BigInteger lastPostId,
    @RequestParam(required = false) Double lastScore,
    @RequestParam(required = false) List<BigInteger> tagIds,
    @RequestParam(required = false, defaultValue = "OR") TagOperador tagOperador,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
  ) {
    FeedRequestDTO request = new FeedRequestDTO(
      usuarioId,
      pageSize,
      lastPostId,
      lastScore,
      tagIds,
      tagOperador,
      dataInicio,
      dataFim
    );
    FeedResponseDTO feed = postService.getFeed(request);
    return ResponseEntity.ok(feed);
  }

  @GetMapping("/buscar")
  public ResponseEntity<PostBuscaResponseDTO> buscarPosts(
    @RequestParam(required = false) List<BigInteger> tagIds,
    @RequestParam(required = false, defaultValue = "OR") TagOperador tagOperador,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
    @RequestParam(required = false, defaultValue = "DATA") OrdenacaoTipo ordenacao,
    @RequestParam(required = false, defaultValue = "DESC") OrdenacaoDirecao direcao,
    @RequestParam(required = false, defaultValue = "20") Integer pageSize,
    @RequestParam(required = false) BigInteger lastPostId,
    @RequestParam(required = false) Long lastValue,
    @RequestParam(required = false) String termo  // ✅ NOVO PARÂMETRO
  ) {
    PostBuscaRequestDTO request = new PostBuscaRequestDTO(
      tagIds,
      tagOperador,
      dataInicio,
      dataFim,
      ordenacao,
      direcao,
      pageSize,
      lastPostId,
      lastValue,
      termo  // ✅ NOVO PARÂMETRO
    );
    PostBuscaResponseDTO resultado = postService.buscarPosts(request);
    return ResponseEntity.ok(resultado);
  }
}
