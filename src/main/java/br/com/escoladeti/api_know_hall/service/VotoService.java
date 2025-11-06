package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.voto.VotoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.Voto;
import br.com.escoladeti.api_know_hall.enums.TipoVoto;
import br.com.escoladeti.api_know_hall.repository.ComentarioRepository;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.VotoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VotoService {

  private final VotoRepository votoRepository;
  private final PostRepository postRepository;
  private final ComentarioRepository comentarioRepository;
  private final UsuarioRepository usuarioRepository;

  @Transactional
  public VotoResponseDTO votarEmPost(BigInteger postId, Principal principal) {
    Post post = postRepository.findById(postId)
      .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));

    Usuario usuario = usuarioRepository.findByEmail(principal.getName())
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    if (post.getUsuario().getId().equals(usuario.getId())) {
      throw new IllegalArgumentException("Você não pode votar no próprio post");
    }

    Optional<Voto> votoExistente = votoRepository.findByPostIdAndUsuarioIdAndTipo(
      postId,
      usuario.getId(),
      TipoVoto.UP_VOTE.name()
    );

    boolean votado;
    if (votoExistente.isPresent()) {
      votoRepository.delete(votoExistente.get());
      votado = false;
    } else {
      Voto novoVoto = new Voto();
      novoVoto.setUsuario(usuario);
      novoVoto.setPost(post);
      novoVoto.setTipo(TipoVoto.UP_VOTE);
      votoRepository.save(novoVoto);
      votado = true;
    }

    Long totalUpVotes = votoRepository.countByPostIdAndTipo(postId, TipoVoto.UP_VOTE.name());

    post.setTotalUpVotes(totalUpVotes);
    postRepository.save(post);

    return new VotoResponseDTO(votado, totalUpVotes);
  }

  @Transactional
  public VotoResponseDTO votarEmComentario(BigInteger comentarioId, Principal principal) {
    Comentario comentario = comentarioRepository.findById(comentarioId)
      .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado"));

    Usuario usuario = usuarioRepository.findByEmail(principal.getName())
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    if (comentario.getUsuario().getId().equals(usuario.getId())) {
      throw new IllegalArgumentException("Você não pode votar no próprio comentário");
    }

    Optional<Voto> votoExistente = votoRepository.findByComentarioIdAndUsuarioIdAndTipo(
      comentarioId,
      usuario.getId(),
      TipoVoto.UP_VOTE.name()
    );

    boolean votado;
    if (votoExistente.isPresent()) {
      votoRepository.delete(votoExistente.get());
      votado = false;
    } else {
      Voto novoVoto = new Voto();
      novoVoto.setUsuario(usuario);
      novoVoto.setComentario(comentario);
      novoVoto.setTipo(TipoVoto.UP_VOTE);
      votoRepository.save(novoVoto);
      votado = true;
    }

    Long totalUpVotes = votoRepository.countByComentarioIdAndTipo(comentarioId, TipoVoto.UP_VOTE.name());

    comentario.setTotalUpVotes(totalUpVotes);
    comentarioRepository.save(comentario);

    return new VotoResponseDTO(votado, totalUpVotes);
  }

  @Transactional
  public VotoResponseDTO superVotarEmComentario(BigInteger comentarioId, Principal principal) {
    Comentario comentario = comentarioRepository.findById(comentarioId)
      .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado"));

    Usuario usuario = usuarioRepository.findByEmail(principal.getName())
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    Post post = comentario.getPost();

    if (!post.getUsuario().getId().equals(usuario.getId())) {
      throw new IllegalArgumentException("Apenas o autor do post pode conceder super votos");
    }

    if (comentario.getUsuario().getId().equals(usuario.getId())) {
      throw new IllegalArgumentException("Você não pode votar no próprio comentário");
    }

    Optional<Voto> superVotoExistente = votoRepository.findByComentarioIdAndUsuarioIdAndTipo(
      comentarioId,
      usuario.getId(),
      TipoVoto.SUPER_VOTE.name()
    );

    boolean votado;

    if (superVotoExistente.isPresent()) {
      votoRepository.delete(superVotoExistente.get());
      votado = false;
    } else {
      Optional<Voto> superVotoEmOutroComentario = votoRepository.findSuperVoteByPostIdAndUsuarioId(
        post.getId(),
        usuario.getId(),
        TipoVoto.SUPER_VOTE.name()
      );

      if (superVotoEmOutroComentario.isPresent()) {
        throw new IllegalArgumentException(
          "Você já concedeu um super voto para outro comentário deste post. " +
            "Remova o super voto anterior para poder conceder um novo."
        );
      }

      Optional<Voto> upVoteExistente = votoRepository.findByComentarioIdAndUsuarioIdAndTipo(
        comentarioId,
        usuario.getId(),
        TipoVoto.UP_VOTE.name()
      );

      upVoteExistente.ifPresent(votoRepository::delete);

      Voto novoVoto = new Voto();
      novoVoto.setUsuario(usuario);
      novoVoto.setComentario(comentario);
      novoVoto.setTipo(TipoVoto.SUPER_VOTE);
      votoRepository.save(novoVoto);
      votado = true;
    }

    Long totalUpVotes = votoRepository.countByComentarioIdAndTipo(comentarioId, TipoVoto.UP_VOTE.name());
    Long totalSuperVotes = votoRepository.countByComentarioIdAndTipo(comentarioId, TipoVoto.SUPER_VOTE.name());

    comentario.setTotalUpVotes(totalUpVotes);
    comentario.setTotalSuperVotes(totalSuperVotes);
    comentarioRepository.save(comentario);

    return new VotoResponseDTO(votado, totalSuperVotes);
  }
}
