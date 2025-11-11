package br.com.escoladeti.api_know_hall.voto;

import br.com.escoladeti.api_know_hall.dto.voto.VotoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.Voto;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoVoto;
import br.com.escoladeti.api_know_hall.repository.ComentarioRepository;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.VotoRepository;
import br.com.escoladeti.api_know_hall.service.HistoricoTransacaoService;
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

  @Mock
  private HistoricoTransacaoService historicoTransacaoService;

  @InjectMocks
  private VotoService votoService;

  private Usuario usuario;
  private Usuario autorPost;
  private Usuario autorComentario;
  private Post post;
  private Comentario comentario;
  private Comentario comentario2;
  private Principal mockPrincipal;
  private Principal mockPrincipalAutorPost;

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
    usuario.setQntdToken(1000L);
    usuario.setQntdXp(1000L);

    autorPost = new Usuario();
    autorPost.setId(BigInteger.TWO);
    autorPost.setNome("Maria Santos");
    autorPost.setEmail("maria@email.com");
    autorPost.setCpf("98765432100");
    autorPost.setSenhaHash("hash456");
    autorPost.setStatusUsuario(StatusUsuario.ATIVO);
    autorPost.setTipoUsuario(TipoUsuario.ALUNO);
    autorPost.setQntdToken(500L);
    autorPost.setQntdXp(500L);

    autorComentario = new Usuario();
    autorComentario.setId(BigInteger.valueOf(3));
    autorComentario.setNome("Pedro Oliveira");
    autorComentario.setEmail("pedro@email.com");
    autorComentario.setCpf("11122233344");
    autorComentario.setSenhaHash("hash789");
    autorComentario.setStatusUsuario(StatusUsuario.ATIVO);
    autorComentario.setTipoUsuario(TipoUsuario.ALUNO);
    autorComentario.setQntdToken(300L);
    autorComentario.setQntdXp(300L);

    post = new Post();
    post.setId(BigInteger.ONE);
    post.setTitulo("Título do Post");
    post.setDescricao("Descrição do post");
    post.setTotalUpVotes(0L);
    post.setMaiorQntdVoto(0L);
    post.setUsuario(autorPost);
    post.setDataCriacao(Timestamp.from(Instant.now()));

    comentario = new Comentario();
    comentario.setId(BigInteger.ONE);
    comentario.setTexto("Comentário de teste");
    comentario.setTotalUpVotes(0L);
    comentario.setTotalSuperVotes(0L);
    comentario.setMaiorQntdVoto(0L);
    comentario.setRespostaDestaque(false);
    comentario.setPost(post);
    comentario.setUsuario(autorComentario);
    comentario.setDataCriacao(Timestamp.from(Instant.now()));

    comentario2 = new Comentario();
    comentario2.setId(BigInteger.TWO);
    comentario2.setTexto("Segundo comentário de teste");
    comentario2.setTotalUpVotes(0L);
    comentario2.setTotalSuperVotes(0L);
    comentario2.setMaiorQntdVoto(0L);
    comentario2.setRespostaDestaque(false);
    comentario2.setPost(post);
    comentario2.setUsuario(autorComentario);
    comentario2.setDataCriacao(Timestamp.from(Instant.now()));

    mockPrincipal = () -> "joao@email.com";
    mockPrincipalAutorPost = () -> "maria@email.com";
  }

  @Test
  @DisplayName("Deve adicionar voto em post com sucesso")
  void deveAdicionarVotoEmPostComSucesso() {
    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
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

    verify(postRepository).findByIdWithUsuario(BigInteger.ONE);
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

    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
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
    when(postRepository.findByIdWithUsuario(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.votarEmPost(BigInteger.valueOf(999), mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Post não encontrado");

    verify(postRepository).findByIdWithUsuario(BigInteger.valueOf(999));
    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar no próprio post")
  void deveLancarExcecaoAoVotarNoProprioPost() {
    post.setUsuario(usuario);

    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> votoService.votarEmPost(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Você não pode votar no próprio post");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar com usuário inexistente")
  void deveLancarExcecaoAoVotarComUsuarioInexistente() {
    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.votarEmPost(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve conceder 100 tokens ao autor do post ao atingir 25 upvotes (primeiro marco)")
  void deveConceder100TokensAoAutorDoPostAoAtingir25Upvotes() {
    post.setMaiorQntdVoto(0L);

    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByPostIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByPostIdAndTipo(any(), any())).thenReturn(25L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmPost(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.TWO) &&
        u.getQntdToken().equals(600L) &&
        u.getQntdXp().equals(600L)
    ));
    verify(historicoTransacaoService).registrarTransacao(
      eq(autorPost),
      eq(100L),
      eq(MotivoTransacao.UP_VOTE_POST),
      anyString()
    );
    verify(postRepository).save(argThat(p -> p.getMaiorQntdVoto().equals(25L)));
  }

  @Test
  @DisplayName("Deve conceder 200 tokens ao autor do post ao atingir 50 upvotes (segundo marco)")
  void deveConceder200TokensAoAutorDoPostAoAtingir50Upvotes() {
    post.setMaiorQntdVoto(25L);

    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByPostIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByPostIdAndTipo(any(), any())).thenReturn(50L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmPost(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.TWO) &&
        u.getQntdToken().equals(600L) &&
        u.getQntdXp().equals(600L)
    ));
    verify(postRepository).save(argThat(p -> p.getMaiorQntdVoto().equals(50L)));
  }

  @Test
  @DisplayName("Não deve conceder tokens ao autor do post se não atingir novo marco")
  void naoDeveConcederTokensAoAutorDoPostSeNaoAtingirNovoMarco() {
    post.setMaiorQntdVoto(25L);

    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByPostIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByPostIdAndTipo(any(), any())).thenReturn(30L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmPost(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository, never()).save(argThat(u ->
      u.getId().equals(BigInteger.TWO)
    ));
    verify(historicoTransacaoService, never()).registrarTransacao(
      any(), any(), eq(MotivoTransacao.UP_VOTE_POST), any()
    );
  }

  @Test
  @DisplayName("Deve conceder 300 tokens ao autor do post ao atingir 75 upvotes (3 marcos)")
  void deveConceder300TokensAoAutorDoPostAoAtingir75Upvotes() {
    post.setMaiorQntdVoto(0L);

    when(postRepository.findByIdWithUsuario(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByPostIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByPostIdAndTipo(any(), any())).thenReturn(75L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmPost(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.TWO) &&
        u.getQntdToken().equals(800L) &&
        u.getQntdXp().equals(800L)
    ));
    verify(historicoTransacaoService).registrarTransacao(
      eq(autorPost),
      eq(300L),
      eq(MotivoTransacao.UP_VOTE_POST),
      anyString()
    );
    verify(postRepository).save(argThat(p -> p.getMaiorQntdVoto().equals(75L)));
  }

  @Test
  @DisplayName("Deve adicionar voto em comentário com sucesso")
  void deveAdicionarVotoEmComentarioComSucesso() {
    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
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

    verify(comentarioRepository).findByIdWithRelations(BigInteger.ONE);
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

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
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
    when(comentarioRepository.findByIdWithRelations(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.votarEmComentario(BigInteger.valueOf(999), mockPrincipal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário não encontrado");

    verify(comentarioRepository).findByIdWithRelations(BigInteger.valueOf(999));
    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao votar no próprio comentário")
  void deveLancarExcecaoAoVotarNoProprioComentario() {
    comentario.setUsuario(usuario);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> votoService.votarEmComentario(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Você não pode votar no próprio comentário");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve conceder 50 tokens ao autor do comentário ao atingir 5 upvotes (primeiro marco)")
  void deveConceder50TokensAoAutorDoComentarioAoAtingir5Upvotes() {
    comentario.setMaiorQntdVoto(0L);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(any(), any())).thenReturn(5L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.valueOf(3)) &&
        u.getQntdToken().equals(350L) &&
        u.getQntdXp().equals(350L)
    ));
    verify(historicoTransacaoService).registrarTransacao(
      eq(autorComentario),
      eq(50L),
      eq(MotivoTransacao.UP_VOTE_COMENTARIO),
      anyString()
    );
    verify(comentarioRepository).save(argThat(c -> c.getMaiorQntdVoto().equals(5L)));
  }

  @Test
  @DisplayName("Deve conceder 100 tokens ao autor do comentário ao atingir 10 upvotes (segundo marco)")
  void deveConceder100TokensAoAutorDoComentarioAoAtingir10Upvotes() {
    comentario.setMaiorQntdVoto(5L);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(any(), any())).thenReturn(10L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.valueOf(3)) &&
        u.getQntdToken().equals(350L) &&
        u.getQntdXp().equals(350L)
    ));
    verify(comentarioRepository).save(argThat(c -> c.getMaiorQntdVoto().equals(10L)));
  }

  @Test
  @DisplayName("Não deve conceder tokens ao autor do comentário se não atingir novo marco")
  void naoDeveConcederTokensAoAutorDoComentarioSeNaoAtingirNovoMarco() {
    comentario.setMaiorQntdVoto(5L);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(any(), any())).thenReturn(7L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository, never()).save(argThat(u ->
      u.getId().equals(BigInteger.valueOf(3))
    ));
    verify(historicoTransacaoService, never()).registrarTransacao(
      any(), any(), eq(MotivoTransacao.UP_VOTE_COMENTARIO), any()
    );
  }

  @Test
  @DisplayName("Deve marcar comentário como Resposta Destaque e conceder 100 tokens ao autor ao atingir 20 upvotes")
  void deveMarcarComoRespostaDestaqueEConceder100TokensAoAtingir20Upvotes() {
    comentario.setMaiorQntdVoto(15L);
    comentario.setRespostaDestaque(false);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(any(), any())).thenReturn(20L);
    when(comentarioRepository.countByPostIdAndRespostaDestaque(BigInteger.ONE, true))
      .thenReturn(0L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    verify(comentarioRepository).save(argThat(c -> c.getRespostaDestaque()));

    verify(usuarioRepository, atLeastOnce()).save(argThat(u ->
      u.getId().equals(BigInteger.valueOf(3)) &&
        u.getQntdToken() >= 400L
    ));

    verify(historicoTransacaoService).registrarTransacao(
      eq(autorComentario),
      eq(100L),
      eq(MotivoTransacao.RESPOSTA_DESTAQUE),
      anyString()
    );
  }

  @Test
  @DisplayName("Deve conceder 100 tokens ao autor do post quando gera primeira Resposta Destaque")
  void deveConceder100TokensAoAutorDoPostQuandoGeraPrimeiraRespostaDestaque() {
    comentario.setMaiorQntdVoto(15L);
    comentario.setRespostaDestaque(false);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(any(), any())).thenReturn(20L);
    when(comentarioRepository.countByPostIdAndRespostaDestaque(BigInteger.ONE, true))
      .thenReturn(0L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository, times(3)).save(any(Usuario.class));

    verify(usuarioRepository, atLeastOnce()).save(argThat(u ->
      u.getId().equals(BigInteger.TWO) &&
        u.getQntdToken().equals(600L)
    ));

    verify(historicoTransacaoService).registrarTransacao(
      eq(autorPost),
      eq(100L),
      eq(MotivoTransacao.GERADOR_QUALIDADE),
      anyString()
    );
  }

  @Test
  @DisplayName("Não deve conceder tokens ao autor do post se já existe resposta destaque")
  void naoDeveConcederTokensAoAutorDoPostSeJaExisteRespostaDestaque() {
    comentario.setMaiorQntdVoto(15L);
    comentario.setRespostaDestaque(false);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(any(), any())).thenReturn(20L);
    when(comentarioRepository.countByPostIdAndRespostaDestaque(BigInteger.ONE, true))
      .thenReturn(1L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    verify(usuarioRepository, times(2)).save(any(Usuario.class));

    verify(historicoTransacaoService, never()).registrarTransacao(
      eq(autorPost),
      eq(100L),
      eq(MotivoTransacao.GERADOR_QUALIDADE),
      anyString()
    );
  }

  @Test
  @DisplayName("Não deve marcar como Resposta Destaque se já estiver marcado")
  void naoDeveMarcarComoRespostaDestaqueSeJaEstiverMarcado() {
    comentario.setMaiorQntdVoto(20L);
    comentario.setRespostaDestaque(true);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(any(), any(), any()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(any(), any())).thenReturn(25L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.votarEmComentario(BigInteger.ONE, mockPrincipal);

    verify(historicoTransacaoService, never()).registrarTransacao(
      any(), eq(100L), eq(MotivoTransacao.RESPOSTA_DESTAQUE), anyString()
    );
  }

  @Test
  @DisplayName("Deve adicionar super voto e conceder 200 tokens ao autor do comentário")
  void deveAdicionarSuperVotoEConceder200TokensAoAutorDoComentario() {
    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(autorPost));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.TWO, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findSuperVoteByPostIdAndUsuarioId(BigInteger.ONE, BigInteger.TWO, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.TWO, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(1L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    VotoResponseDTO resultado = votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipalAutorPost);

    assertThat(resultado.votado()).isTrue();
    assertThat(resultado.totalUpVotes()).isEqualTo(1L);

    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.valueOf(3)) &&
        u.getQntdToken().equals(500L) &&
        u.getQntdXp().equals(500L)
    ));
    verify(historicoTransacaoService).registrarTransacao(
      eq(autorComentario),
      eq(200L),
      eq(MotivoTransacao.SUPER_VOTE),
      anyString()
    );
  }

  @Test
  @DisplayName("Deve remover super voto e remover 200 tokens do autor do comentário")
  void deveRemoverSuperVotoERemover200TokensDoAutorDoComentario() {
    Voto superVotoExistente = new Voto();
    superVotoExistente.setId(BigInteger.ONE);
    superVotoExistente.setUsuario(autorPost);
    superVotoExistente.setComentario(comentario);
    superVotoExistente.setTipo(TipoVoto.SUPER_VOTE);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(autorPost));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.TWO, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.of(superVotoExistente));
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(0L);
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    VotoResponseDTO resultado = votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipalAutorPost);

    assertThat(resultado.votado()).isFalse();
    assertThat(resultado.totalUpVotes()).isEqualTo(0L);

    verify(votoRepository).delete(superVotoExistente);
    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.valueOf(3)) &&
        u.getQntdToken().equals(100L) &&
        u.getQntdXp().equals(100L)
    ));
    verify(historicoTransacaoService).registrarTransacao(
      eq(autorComentario),
      eq(-200L),
      eq(MotivoTransacao.SUPER_VOTE),
      anyString()
    );
  }

  @Test
  @DisplayName("Deve substituir UP_VOTE por SUPER_VOTE ao super votar")
  void deveSubstituirUpVotePorSuperVoteAoSuperVotar() {
    Voto upVoteExistente = new Voto();
    upVoteExistente.setId(BigInteger.ONE);
    upVoteExistente.setUsuario(autorPost);
    upVoteExistente.setComentario(comentario);
    upVoteExistente.setTipo(TipoVoto.UP_VOTE);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(autorPost));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.TWO, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findSuperVoteByPostIdAndUsuarioId(BigInteger.ONE, BigInteger.TWO, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.TWO, TipoVoto.UP_VOTE.name()))
      .thenReturn(Optional.of(upVoteExistente));
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.UP_VOTE.name()))
      .thenReturn(0L);
    when(votoRepository.countByComentarioIdAndTipo(BigInteger.ONE, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(1L);
    when(votoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(comentarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipalAutorPost);

    verify(votoRepository).delete(upVoteExistente);
    verify(votoRepository).save(argThat(v -> v.getTipo() == TipoVoto.SUPER_VOTE));
    verify(usuarioRepository).save(argThat(u ->
      u.getId().equals(BigInteger.valueOf(3)) &&
        u.getQntdToken().equals(500L)
    ));
  }

  @Test
  @DisplayName("Deve lançar exceção ao super votar em comentário inexistente")
  void deveLancarExcecaoAoSuperVotarEmComentarioInexistente() {
    when(comentarioRepository.findByIdWithRelations(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votoService.superVotarEmComentario(BigInteger.valueOf(999), mockPrincipalAutorPost))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário não encontrado");

    verify(comentarioRepository).findByIdWithRelations(BigInteger.valueOf(999));
    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao super votar no próprio comentário")
  void deveLancarExcecaoAoSuperVotarNoProprioComentario() {
    comentario.setUsuario(autorPost);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(autorPost));

    assertThatThrownBy(() -> votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipalAutorPost))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Você não pode votar no próprio comentário");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar super votar sem ser autor do post")
  void deveLancarExcecaoAoTentarSuperVotarSemSerAutorDoPost() {
    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipal))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Apenas o autor do post pode conceder super votos");

    verify(votoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar super votar quando já existe super voto em outro comentário")
  void deveLancarExcecaoAoTentarSuperVotarQuandoJaExisteSuperVotoEmOutroComentario() {
    Voto superVotoExistenteEmOutroComentario = new Voto();
    superVotoExistenteEmOutroComentario.setId(BigInteger.valueOf(10));
    superVotoExistenteEmOutroComentario.setUsuario(autorPost);
    superVotoExistenteEmOutroComentario.setComentario(comentario2);
    superVotoExistenteEmOutroComentario.setTipo(TipoVoto.SUPER_VOTE);

    when(comentarioRepository.findByIdWithRelations(BigInteger.ONE)).thenReturn(Optional.of(comentario));
    when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(autorPost));
    when(votoRepository.findByComentarioIdAndUsuarioIdAndTipo(BigInteger.ONE, BigInteger.TWO, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.empty());
    when(votoRepository.findSuperVoteByPostIdAndUsuarioId(BigInteger.ONE, BigInteger.TWO, TipoVoto.SUPER_VOTE.name()))
      .thenReturn(Optional.of(superVotoExistenteEmOutroComentario));

    assertThatThrownBy(() -> votoService.superVotarEmComentario(BigInteger.ONE, mockPrincipalAutorPost))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Você já concedeu um super voto para outro comentário deste post");

    verify(votoRepository, never()).save(any());
  }
}
