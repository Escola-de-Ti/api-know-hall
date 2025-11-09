package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.voto.VotoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.Voto;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
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
  private final HistoricoTransacaoService historicoTransacaoService;

  @Transactional
  public VotoResponseDTO votarEmPost(BigInteger postId, Principal principal) {
    Post post = postRepository.findByIdWithUsuario(postId)
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

    if (votado) {
      Long maiorQntdVoto = post.getMaiorQntdVoto() != null ? post.getMaiorQntdVoto() : 0L;

      if (totalUpVotes > maiorQntdVoto) {
        Usuario autorPost = post.getUsuario();
        concederTokensPostPopular(autorPost, maiorQntdVoto, totalUpVotes, post);
        post.setMaiorQntdVoto(totalUpVotes);
      }
    }

    postRepository.save(post);
    return new VotoResponseDTO(votado, totalUpVotes);
  }

  @Transactional
  public VotoResponseDTO votarEmComentario(BigInteger comentarioId, Principal principal) {
    Comentario comentario = comentarioRepository.findByIdWithRelations(comentarioId)
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

    if (votado) {
      Long maiorQntdVoto = comentario.getMaiorQntdVoto() != null ? comentario.getMaiorQntdVoto() : 0L;

      if (totalUpVotes > maiorQntdVoto) {
        Usuario autorComentario = comentario.getUsuario();
        Post post = comentario.getPost();
        Usuario autorPost = post.getUsuario();

        concederTokensMarcosComentario(autorComentario, maiorQntdVoto, totalUpVotes, comentario);
        marcarEConcederTokensRespostaDestaque(comentario, maiorQntdVoto, totalUpVotes, autorComentario, post, autorPost);

        comentario.setMaiorQntdVoto(totalUpVotes);
      }
    }

    comentarioRepository.save(comentario);
    return new VotoResponseDTO(votado, totalUpVotes);
  }

  @Transactional
  public VotoResponseDTO superVotarEmComentario(BigInteger comentarioId, Principal principal) {
    Comentario comentario = comentarioRepository.findByIdWithRelations(comentarioId)
      .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado"));

    Usuario usuario = usuarioRepository.findByEmail(principal.getName())
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    Post post = comentario.getPost();
    Usuario autorPost = post.getUsuario();

    if (!autorPost.getId().equals(usuario.getId())) {
      throw new IllegalArgumentException("Apenas o autor do post pode conceder super votos");
    }

    Usuario autorComentario = comentario.getUsuario();

    if (autorComentario.getId().equals(usuario.getId())) {
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

      removerTokensSuperVote(autorComentario, comentario);
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

      concederTokensSuperVote(autorComentario, comentario);
    }

    Long totalUpVotes = votoRepository.countByComentarioIdAndTipo(comentarioId, TipoVoto.UP_VOTE.name());
    Long totalSuperVotes = votoRepository.countByComentarioIdAndTipo(comentarioId, TipoVoto.SUPER_VOTE.name());

    comentario.setTotalUpVotes(totalUpVotes);
    comentario.setTotalSuperVotes(totalSuperVotes);
    comentarioRepository.save(comentario);

    return new VotoResponseDTO(votado, totalSuperVotes);
  }

  private void concederTokensPostPopular(Usuario autorPost, Long totalAnterior, Long totalAtual, Post post) {
    long marcosAntes = totalAnterior / 25;
    long marcosAgora = totalAtual / 25;

    long novosMarcos = marcosAgora - marcosAntes;

    if (novosMarcos > 0) {
      long tokensGanhos = novosMarcos * 100;

      autorPost.setQntdToken(autorPost.getQntdToken() + tokensGanhos);
      autorPost.setQntdXp(autorPost.getQntdXp() + tokensGanhos);
      usuarioRepository.save(autorPost);

      String descricao = String.format(
        truncarTexto(post.getTitulo(), 50),
        post.getId(),
        totalAtual,
        novosMarcos
      );

      historicoTransacaoService.registrarTransacao(
        autorPost,
        tokensGanhos,
        MotivoTransacao.UP_VOTE_POST,
        descricao
      );
    }
  }

  private void concederTokensMarcosComentario(Usuario autorComentario, Long totalAnterior, Long totalAtual, Comentario comentario) {
    long marcosAntes = totalAnterior / 5;
    long marcosAgora = totalAtual / 5;

    long novosMarcos = marcosAgora - marcosAntes;

    if (novosMarcos > 0) {
      long tokensGanhos = novosMarcos * 50;

      autorComentario.setQntdToken(autorComentario.getQntdToken() + tokensGanhos);
      autorComentario.setQntdXp(autorComentario.getQntdXp() + tokensGanhos);
      usuarioRepository.save(autorComentario);

      String descricao = String.format(
        "Comentário '%s...' (ID: %s) atingiu %d upvotes - %d marcos de 5 conquistados",
        truncarTexto(comentario.getTexto(), 30),
        comentario.getId(),
        totalAtual,
        novosMarcos
      );

      historicoTransacaoService.registrarTransacao(
        autorComentario,
        tokensGanhos,
        MotivoTransacao.UP_VOTE_COMENTARIO,
        descricao
      );
    }
  }

  private void marcarEConcederTokensRespostaDestaque(
    Comentario comentario,
    Long totalAnterior,
    Long totalAtual,
    Usuario autorComentario,
    Post post,
    Usuario autorPost) {

    if (totalAnterior < 20 && totalAtual >= 20 && !comentario.getRespostaDestaque()) {

      comentario.setRespostaDestaque(true);

      autorComentario.setQntdToken(autorComentario.getQntdToken() + 100);
      autorComentario.setQntdXp(autorComentario.getQntdXp() + 100);
      usuarioRepository.save(autorComentario);

      String descricaoComentario = String.format(
        "Comentário '%s...' (ID: %s) marcado como Resposta Destaque com 20 upvotes",
        truncarTexto(comentario.getTexto(), 30),
        comentario.getId()
      );

      historicoTransacaoService.registrarTransacao(
        autorComentario,
        100L,
        MotivoTransacao.RESPOSTA_DESTAQUE,
        descricaoComentario
      );

      long totalRespostasDestaque = comentarioRepository.countByPostIdAndRespostaDestaque(
        post.getId(),
        true
      );

      if (totalRespostasDestaque == 0) {
        autorPost.setQntdToken(autorPost.getQntdToken() + 100);
        autorPost.setQntdXp(autorPost.getQntdXp() + 100);
        usuarioRepository.save(autorPost);

        String descricaoPost = String.format(
          "Post '%s' (ID: %s) gerou primeira Resposta Destaque",
          truncarTexto(post.getTitulo(), 50),
          post.getId()
        );

        historicoTransacaoService.registrarTransacao(
          autorPost,
          100L,
          MotivoTransacao.GERADOR_QUALIDADE,
          descricaoPost
        );
      }
    }
  }

  private void concederTokensSuperVote(Usuario autorComentario, Comentario comentario) {
    autorComentario.setQntdToken(autorComentario.getQntdToken() + 200);
    autorComentario.setQntdXp(autorComentario.getQntdXp() + 200);
    usuarioRepository.save(autorComentario);

    String descricao = String.format(
      "Comentário '%s...' (ID: %s) recebeu Super Vote do autor do post",
      truncarTexto(comentario.getTexto(), 30),
      comentario.getId()
    );

    historicoTransacaoService.registrarTransacao(
      autorComentario,
      200L,
      MotivoTransacao.SUPER_VOTE,
      descricao
    );
  }

  private void removerTokensSuperVote(Usuario autorComentario, Comentario comentario) {
    autorComentario.setQntdToken(autorComentario.getQntdToken() - 200);
    autorComentario.setQntdXp(autorComentario.getQntdXp() - 200);
    usuarioRepository.save(autorComentario);

    String descricao = String.format(
      "Super Vote removido do comentário '%s...' (ID: %s)",
      truncarTexto(comentario.getTexto(), 30),
      comentario.getId()
    );

    historicoTransacaoService.registrarTransacao(
      autorComentario,
      -200L,
      MotivoTransacao.SUPER_VOTE,
      descricao
    );
  }

  private String truncarTexto(String texto, int tamanhoMaximo) {
    if (texto == null) return "";
    if (texto.length() <= tamanhoMaximo) return texto;
    return texto.substring(0, tamanhoMaximo) + "...";
  }
}
