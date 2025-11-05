package br.com.escoladeti.api_know_hall.voto;

import br.com.escoladeti.api_know_hall.dto.voto.VotoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.Voto;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoVoto;
import br.com.escoladeti.api_know_hall.repository.ComentarioRepository;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.VotoRepository;
import br.com.escoladeti.api_know_hall.service.VotoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do VotoService")
class VotoServiceTest {

  @Mock
  private VotoRepository votoRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private ComentarioRepository comentarioRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private VotoService votoService;

  private Usuario usuario;
  private Usuario autorPost;
  private Post post;
  private Comentario comentario;
  private Principal mockPrincipal;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(BigInteger.ONE);
    usuario.setNome("João Silva");
    usuario.setEmail("joao@email.com");
    usuario.setCpf("12345678901");
    usuario.setSenhaHash("hash123");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);

    autorPost = new Usuario();
    autorPost.setId(BigInteger.TWO);
    autorPost.setNome("Maria Santos");
    autorPost.setEmail("maria@email.com");
    autorPost.setCpf("98765432100");
    autorPost.setSenhaHash("hash456");
    autorPost.setStatusUsuario(StatusUsuario.ATIVO);
    autorPost.setTipoUsuario(TipoUsuario.ALUNO);

    post = new Post();
    post.setId(BigInteger.ONE);
    post.setTitulo("Título do Post");
    post.setDescricao("Descrição do post");
    post.setTotalUpVotes(0L);
    post.setUsuario(autorPost);
    post.setDataCriacao(Timestamp.from(Instant.now()));

    comentario = new Comentario();
    comentario.setId(BigInteger.ONE);
    comentario.setTexto("Comentário de teste");
    comentario.setTotalUpVotes(0L);
    comentario.setTotalSuperVotes(0L);
    comentario.setPost(post);
    comentario.setUsuario(autorPost);
    comentario.setDataCriacao(Timestamp.from(Instant.now()));

    mockPrincipal = () -> "joao@email.com";
  }

  // ==================== TESTES DE UP_VOTE EM POST ====================

  @Test
  @DisplayName("Deve adicionar voto em post com sucesso")
  void deveAdicionarVotoEmPostComSucesso() {
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByPostIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByPostIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(1L);
    when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.votarEmPost(BigInteger.ONE, mockPrincipal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.votado()).isTrue();
    assertThat(resultado.totalUpVotes()).isEqualTo(1L);

    verify(postRepository).findById(BigInteger.ONE);
    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(votoRepository).save(any(Voto.class));
    verify(postRepository).save(argThat(p -> p.getTotalUpVotes().equals(1L)));
  }

  @Test
  @DisplayName("Deve remover voto em post com sucesso")
  void deveRemoverVotoEmPostComSucesso() {
    Voto votoExistente = new Voto();
    votoExistente.setId(BigInteger.ONE);
    votoExistente.setUsuario(usuario);
    votoExistente.setPost(post);
    votoExistente.setTipo(TipoVoto.UP_VOTE);

    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByPostIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.of(votoExistente));
    when(votoRepository.countByPostIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.votarEmPost(BigInteger.ONE, mockPrincipal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.votado()).isFalse();
    assertThat(resultado.totalUpVotes()).isEqualTo(0L);

    verify(votoRepository).delete(votoExistente);
    verify(postRepository).save(argThat(p -> p.getTotalUpVotes().equals(0L)));
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar em post inexistente")
  void deveLancarExcecaoAoVotarEmPostInexistente() {
    when(postRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.votarEmPost(BigInteger.valueOf(999), mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Post não encontrado");

    verify(postRepository).findById(BigInteger.valueOf(999));
    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar no próprio post")
  void deveLancarExcecaoAoVotarNoProprioPost() {
    post.setUsuario(usuario);

    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> votoService.votarEmPost(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Você não pode votar no próprio post");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar com usuário inexistente")
  void deveLancarExcecaoAoVotarComUsuarioInexistente() {
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.votarEmPost(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve atualizar totalUpVotes corretamente após múltiplos votos em post")
  void deveAtualizarTotalUpVotesAposMultiplosVotosEmPost() {
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByPostIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByPostIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(5L);
    when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.votarEmPost(BigInteger.ONE, mockPrincipal);

    assertThat(resultado.totalUpVotes()).isEqualTo(5L);
    verify(postRepository).save(argThat(p -> p.getTotalUpVotes().equals(5L)));
  }

  // ==================== TESTES DE UP_VOTE EM COMENTÁRIO ====================

  @Test
  @DisplayName("Deve adicionar voto em comentário com sucesso")
  void deveAdicionarVotoEmComentarioComSucesso() {
    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(1L);
    when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.votado()).isTrue();
    assertThat(resultado.totalUpVotes()).isEqualTo(1L);

    verify(comentarioRepository).findById(BigInteger.ONE);
    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(votoRepository).save(any(Voto.class));
    verify(comentarioRepository).save(argThat(c -> c.getTotalUpVotes().equals(1L)));
  }

  @Test
  @DisplayName("Deve remover voto em comentário com sucesso")
  void deveRemoverVotoEmComentarioComSucesso() {
    Voto votoExistente = new Voto();
    votoExistente.setId(BigInteger.ONE);
    votoExistente.setUsuario(usuario);
    votoExistente.setComentario(comentario);
    votoExistente.setTipo(TipoVoto.UP_VOTE);

    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.of(votoExistente));
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.votado()).isFalse();
    assertThat(resultado.totalUpVotes()).isEqualTo(0L);

    verify(votoRepository).delete(votoExistente);
    verify(comentarioRepository).save(argThat(c -> c.getTotalUpVotes().equals(0L)));
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar em comentário inexistente")
  void deveLancarExcecaoAoVotarEmComentarioInexistente() {
    when(comentarioRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.votarEmComentario(BigInteger.valueOf(999), mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário não encontrado");

    verify(comentarioRepository).findById(BigInteger.valueOf(999));
    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar no próprio comentário")
  void deveLancarExcecaoAoVotarNoProprioComentario() {
    comentario.setUsuario(usuario);

    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> votoService.votarEmComentario(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Você não pode votar no próprio comentário");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve atualizar totalUpVotes corretamente após múltiplos votos em comentário")
  void deveAtualizarTotalUpVotesAposMultiplosVotosEmComentario() {
    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(3L);
    when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    assertThat(resultado.totalUpVotes()).isEqualTo(3L);
    verify(comentarioRepository).save(argThat(c -> c.getTotalUpVotes().equals(3L)));
  }

  // ==================== TESTES DE SUPER_VOTE EM COMENTÁRIO ====================

  @Test
  @DisplayName("Deve adicionar super voto em comentário com sucesso")
  void deveAdicionarSuperVotoEmComentarioComSucesso() {
    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(1L);
    when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.votado()).isTrue();
    assertThat(resultado.totalUpVotes()).isEqualTo(1L);

    verify(comentarioRepository).findById(BigInteger.ONE);
    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(votoRepository).save(argThat(v -> v.getTipo() == TipoVoto.SUPER_VOTE));
    verify(comentarioRepository).save(argThat(c ->
      c.getTotalUpVotes().equals(0L) && c.getTotalSuperVotes().equals(1L)
    ));
  }

  @Test
  @DisplayName("Deve remover super voto em comentário com sucesso")
  void deveRemoverSuperVotoEmComentarioComSucesso() {
    Voto superVotoExistente = new Voto();
    superVotoExistente.setId(BigInteger.ONE);
    superVotoExistente.setUsuario(usuario);
    superVotoExistente.setComentario(comentario);
    superVotoExistente.setTipo(TipoVoto.SUPER_VOTE);

    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.of(superVotoExistente));
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(0L);
    when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.votado()).isFalse();
    assertThat(resultado.totalUpVotes()).isEqualTo(0L);

    verify(votoRepository).delete(superVotoExistente);
    verify(comentarioRepository).save(argThat(c ->
      c.getTotalUpVotes().equals(0L) && c.getTotalSuperVotes().equals(0L)
    ));
  }

  @Test
  @DisplayName("Deve substituir UP_VOTE por SUPER_VOTE ao super votar")
  void deveSubstituirUpVotePorSuperVoteAoSuperVotar() {
    Voto upVoteExistente = new Voto();
    upVoteExistente.setId(BigInteger.ONE);
    upVoteExistente.setUsuario(usuario);
    upVoteExistente.setComentario(comentario);
    upVoteExistente.setTipo(TipoVoto.UP_VOTE);

    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.of(upVoteExistente));
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(1L);
    when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.votado()).isTrue();
    assertThat(resultado.totalUpVotes()).isEqualTo(1L);

    verify(votoRepository).delete(upVoteExistente);
    verify(votoRepository).save(argThat(v -> v.getTipo() == TipoVoto.SUPER_VOTE));
    verify(comentarioRepository).save(argThat(c ->
      c.getTotalUpVotes().equals(0L) && c.getTotalSuperVotes().equals(1L)
    ));
  }

  @Test
  @DisplayName("Deve lançar exceção ao super votar em comentário inexistente")
  void deveLancarExcecaoAoSuperVotarEmComentarioInexistente() {
    when(comentarioRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.superVotarEmComentario(BigInteger.valueOf(999), mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário não encontrado");

    verify(comentarioRepository).findById(BigInteger.valueOf(999));
    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao super votar no próprio comentário")
  void deveLancarExcecaoAoSuperVotarNoProprioComentario() {
    comentario.setUsuario(usuario);

    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Você não pode votar no próprio comentário");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve atualizar totalSuperVotes corretamente após múltiplos super votos")
  void deveAtualizarTotalSuperVotesAposMultiplosSuperVotos() {
    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(2L);
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(3L);
    when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VotoResponseDTO resultado = votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipal);

    assertThat(resultado.totalUpVotes()).isEqualTo(3L);
    verify(comentarioRepository).save(argThat(c ->
      c.getTotalUpVotes().equals(2L) && c.getTotalSuperVotes().equals(3L)
    ));
  }

  @Test
  @DisplayName("Deve lançar exceção ao super votar com usuário inexistente")
  void deveLancarExcecaoAoSuperVotarComUsuarioInexistente() {
    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(votoRepository, never()).save(any());
  }
}
