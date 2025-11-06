package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioListResponseDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioResponseDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.projection.comentario.ComentarioProjection;
import br.com.escoladeti.api_know_hall.repository.ComentarioRepository;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

  private final ComentarioRepository comentarioRepository;
  private final PostRepository postRepository;
  private final UsuarioRepository usuarioRepository;

  @Transactional
  public ComentarioResponseDTO criarComentario(ComentarioCreateDTO dto, Principal principal) {
    Usuario usuario = buscarUsuarioPorPrincipal(principal);

    Post post = postRepository.findById(dto.postId())
      .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));

    Comentario comentario = new Comentario();
    comentario.setPost(post);
    comentario.setUsuario(usuario);
    comentario.setTexto(dto.texto());
    comentario.setTotalUpVotes(0L);
    comentario.setTotalSuperVotes(0L);

    if (dto.comentarioPaiId() != null) {
      Comentario comentarioPai = comentarioRepository.findById(dto.comentarioPaiId())
        .orElseThrow(() -> new EntityNotFoundException("Comentário pai não encontrado"));

      if (!comentarioPai.getPost().getId().equals(dto.postId())) {
        throw new IllegalArgumentException("Comentário pai não pertence ao post informado");
      }

      comentario.setComentarioPai(comentarioPai);
    }

    Comentario comentarioSalvo = comentarioRepository.save(comentario);
    return mapToResponseDTO(comentarioSalvo);
  }

  @Transactional(readOnly = true)
  public ComentarioListResponseDTO buscarComentariosDoPost(
    BigInteger postId,
    BigInteger lastComentarioId,
    Integer pageSize
  ) {
    if (!postRepository.existsById(postId)) {
      throw new EntityNotFoundException("Post não encontrado");
    }

    Integer fetchSize = pageSize + 1;

    List<ComentarioProjection> results = comentarioRepository.findComentariosByPostId(
      postId,
      lastComentarioId,
      fetchSize
    );

    boolean hasMore = results.size() > pageSize;

    if (hasMore) {
      results = results.subList(0, pageSize);
    }

    List<ComentarioResponseDTO> comentarios = results.stream()
      .map(this::mapProjectionToDTO)
      .collect(Collectors.toList());

    BigInteger ultimoId = comentarios.isEmpty() ? null : comentarios.get(comentarios.size() - 1).id();

    return new ComentarioListResponseDTO(comentarios, hasMore, ultimoId);
  }

  @Transactional(readOnly = true)
  public ComentarioListResponseDTO buscarRespostasDoComentario(
    BigInteger comentarioPaiId,
    BigInteger lastComentarioId,
    Integer pageSize
  ) {
    if (!comentarioRepository.existsById(comentarioPaiId)) {
      throw new EntityNotFoundException("Comentário não encontrado");
    }

    Integer fetchSize = pageSize + 1;

    List<ComentarioProjection> results = comentarioRepository.findRespostasByComentarioPaiId(
      comentarioPaiId,
      lastComentarioId,
      fetchSize
    );

    boolean hasMore = results.size() > pageSize;

    if (hasMore) {
      results = results.subList(0, pageSize);
    }

    List<ComentarioResponseDTO> comentarios = results.stream()
      .map(this::mapProjectionToDTO)
      .collect(Collectors.toList());

    BigInteger ultimoId = comentarios.isEmpty() ? null : comentarios.get(comentarios.size() - 1).id();

    return new ComentarioListResponseDTO(comentarios, hasMore, ultimoId);
  }

  @Transactional(readOnly = true)
  public ComentarioListResponseDTO buscarComentariosDoUsuario(
    Principal principal,
    BigInteger lastComentarioId,
    Integer pageSize
  ) {
    Usuario usuario = buscarUsuarioPorPrincipal(principal);

    Integer fetchSize = pageSize + 1;

    List<ComentarioProjection> results = comentarioRepository.findComentariosByUsuarioId(
      usuario.getId(),
      lastComentarioId,
      fetchSize
    );

    boolean hasMore = results.size() > pageSize;

    if (hasMore) {
      results = results.subList(0, pageSize);
    }

    List<ComentarioResponseDTO> comentarios = results.stream()
      .map(this::mapProjectionToDTO)
      .collect(Collectors.toList());

    BigInteger ultimoId = comentarios.isEmpty() ? null : comentarios.get(comentarios.size() - 1).id();

    return new ComentarioListResponseDTO(comentarios, hasMore, ultimoId);
  }

  @Transactional
  public ComentarioResponseDTO atualizarComentario(
    BigInteger id,
    ComentarioUpdateDTO dto,
    Principal principal
  ) {
    Usuario usuario = buscarUsuarioPorPrincipal(principal);

    Comentario comentario = comentarioRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado"));

    if (!comentario.getUsuario().getId().equals(usuario.getId())) {
      throw new AccessDeniedException("Você não tem permissão para editar este comentário");
    }

    comentario.setTexto(dto.texto());

    Comentario comentarioAtualizado = comentarioRepository.save(comentario);
    return mapToResponseDTO(comentarioAtualizado);
  }

  @Transactional
  public void deletarComentario(BigInteger id, Principal principal) {
    Usuario usuario = buscarUsuarioPorPrincipal(principal);

    Comentario comentario = comentarioRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado"));

    if (!comentario.getUsuario().getId().equals(usuario.getId())) {
      throw new AccessDeniedException("Você não tem permissão para deletar este comentário");
    }

    comentarioRepository.deleteById(id);
  }

  private Usuario buscarUsuarioPorPrincipal(Principal principal) {
    String email = principal.getName();
    return usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
  }

  private ComentarioResponseDTO mapToResponseDTO(Comentario comentario) {
    return new ComentarioResponseDTO(
      comentario.getId(),
      comentario.getPost().getId(),
      comentario.getUsuario().getId(),
      comentario.getUsuario().getNome(),
      comentario.getTexto(),
      comentario.getTotalUpVotes(),
      comentario.getTotalSuperVotes(),
      comentario.getComentarioPai() != null ? comentario.getComentarioPai().getId() : null,
      comentario.getDataCriacao()
    );
  }

  private ComentarioResponseDTO mapProjectionToDTO(ComentarioProjection projection) {
    return new ComentarioResponseDTO(
      projection.getId(),
      projection.getPostId(),
      projection.getUsuarioId(),
      projection.getUsuarioNome(),
      projection.getTexto(),
      projection.getTotalUpVotes(),
      projection.getTotalSuperVotes(),
      projection.getComentarioPaiId(),
      projection.getDataCriacao()
    );
  }
}
