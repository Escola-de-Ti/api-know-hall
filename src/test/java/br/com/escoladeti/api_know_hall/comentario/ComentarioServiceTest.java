package br.com.escoladeti.api_know_hall.comentario;

import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioListResponseDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioResponseDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.projection.comentario.ComentarioProjection;
import br.com.escoladeti.api_know_hall.repository.ComentarioRepository;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.ComentarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigInteger;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes da ComentarioService")
class ComentarioServiceTest {

  @Mock
  private ComentarioRepository comentarioRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private Principal principal;

  @InjectMocks
  private ComentarioService comentarioService;

  private Usuario usuario;
  private Post post;
  private Comentario comentario;
  private Comentario comentarioPai;

  @BeforeEach
  void setUp() {
    // Setup Usuario
    usuario = new Usuario();
    usuario.setId(BigInteger.ONE);
    usuario.setNome("João Silva");
    usuario.setEmail("joao@email.com");
    usuario.setCpf("12345678901");
    usuario.setSenhaHash("hash123");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);

    // Setup Post
    post = new Post();
    post.setId(BigInteger.ONE);
    post.setTitulo("Post de Teste");
    post.setDescricao("Descrição do post");
    post.setTotalUpVotes(10L);
    post.setUsuario(usuario);
    post.setComentarios(new ArrayList<>());
    post.setDataCriacao(Timestamp.from(Instant.now()));

    // Setup Comentario Pai
    comentarioPai = new Comentario();
    comentarioPai.setId(BigInteger.ONE);
    comentarioPai.setTexto("Comentário pai");
    comentarioPai.setPost(post);
    comentarioPai.setUsuario(usuario);
    comentarioPai.setTotalUpVotes(5L);
    comentarioPai.setTotalSuperVotes(2L);
    comentarioPai.setRespostas(new ArrayList<>());
    comentarioPai.setDataCriacao(Timestamp.from(Instant.now()));

    // Setup Comentario
    comentario = new Comentario();
    comentario.setId(BigInteger.TWO);
    comentario.setTexto("Comentário de teste");
    comentario.setPost(post);
    comentario.setUsuario(usuario);
    comentario.setTotalUpVotes(0L);
    comentario.setTotalSuperVotes(0L);
    comentario.setRespostas(new ArrayList<>());
    comentario.setDataCriacao(Timestamp.from(Instant.now()));
  }

  @Test
  @DisplayName("Deve criar comentário principal com sucesso")
  void deveCriarComentarioPrincipalComSucesso() {
    ComentarioCreateDTO dto = new ComentarioCreateDTO(
      BigInteger.ONE,
      "Novo comentário",
      null
    );

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

    ComentarioResponseDTO resultado = comentarioService.criarComentario(dto, principal);

    assertThat(resultado).isNotNull();
    assertThat(resultado.postId()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.usuarioId()).isEqualTo(BigInteger.ONE);

    verify(comentarioRepository, times(1)).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve criar resposta a comentário com sucesso")
  void deveCriarRespostaAComentarioComSucesso() {
    ComentarioCreateDTO dto = new ComentarioCreateDTO(
      BigInteger.ONE,
      "Resposta ao comentário",
      BigInteger.ONE
    );

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentarioPai));
    when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

    ComentarioResponseDTO resultado = comentarioService.criarComentario(dto, principal);

    assertThat(resultado).isNotNull();
    verify(comentarioRepository, times(1)).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar comentário com usuário não encontrado")
  void deveLancarExcecaoAoCriarComentarioComUsuarioNaoEncontrado() {
    ComentarioCreateDTO dto = new ComentarioCreateDTO(
      BigInteger.ONE,
      "Novo comentário",
      null
    );

    when(principal.getName()).thenReturn("invalido@email.com");
    when(usuarioRepository.findByEmail("invalido@email.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> comentarioService.criarComentario(dto, principal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(comentarioRepository, never()).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar comentário com post não encontrado")
  void deveLancarExcecaoAoCriarComentarioComPostNaoEncontrado() {
    ComentarioCreateDTO dto = new ComentarioCreateDTO(
      BigInteger.valueOf(999),
      "Novo comentário",
      null
    );

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> comentarioService.criarComentario(dto, principal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Post não encontrado");

    verify(comentarioRepository, never()).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar resposta com comentário pai não encontrado")
  void deveLancarExcecaoAoCriarRespostaComComentarioPaiNaoEncontrado() {
    ComentarioCreateDTO dto = new ComentarioCreateDTO(
      BigInteger.ONE,
      "Resposta",
      BigInteger.valueOf(999)
    );

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(comentarioRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> comentarioService.criarComentario(dto, principal))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário pai não encontrado");

    verify(comentarioRepository, never()).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar resposta com comentário pai de outro post")
  void deveLancarExcecaoAoCriarRespostaComComentarioPaiDeOutroPost() {
    Post outroPost = new Post();
    outroPost.setId(BigInteger.TWO);

    Comentario comentarioPaiOutroPost = new Comentario();
    comentarioPaiOutroPost.setId(BigInteger.ONE);
    comentarioPaiOutroPost.setPost(outroPost);

    ComentarioCreateDTO dto = new ComentarioCreateDTO(
      BigInteger.ONE,
      "Resposta",
      BigInteger.ONE
    );

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(comentarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(comentarioPaiOutroPost));

    assertThatThrownBy(() -> comentarioService.criarComentario(dto, principal))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Comentário pai não pertence ao post informado");

    verify(comentarioRepository, never()).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve buscar comentários do post com sucesso")
  void deveBuscarComentariosDoPostComSucesso() {
    List<ComentarioProjection> projections = List.of(
      createComentarioProjection(BigInteger.ONE),
      createComentarioProjection(BigInteger.TWO)
    );

    when(postRepository.existsById(BigInteger.ONE)).thenReturn(true);
    when(comentarioRepository.findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(21)))
      .thenReturn(projections);

    ComentarioListResponseDTO resultado = comentarioService.buscarComentariosDoPost(
      BigInteger.ONE,
      null,
      20
    );

    assertThat(resultado).isNotNull();
    assertThat(resultado.comentarios()).hasSize(2);
    assertThat(resultado.hasMore()).isFalse();

    verify(comentarioRepository, times(1))
      .findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(21));
  }

  @Test
  @DisplayName("Deve indicar que há mais comentários quando ultrapassar o pageSize")
  void deveIndicarQueHaMaisComentariosQuandoUltrapassarPageSize() {
    List<ComentarioProjection> projections = List.of(
      createComentarioProjection(BigInteger.ONE),
      createComentarioProjection(BigInteger.TWO),
      createComentarioProjection(BigInteger.valueOf(3))
    );

    when(postRepository.existsById(BigInteger.ONE)).thenReturn(true);
    when(comentarioRepository.findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(3)))
      .thenReturn(projections);

    ComentarioListResponseDTO resultado = comentarioService.buscarComentariosDoPost(
      BigInteger.ONE,
      null,
      2
    );

    assertThat(resultado).isNotNull();
    assertThat(resultado.comentarios()).hasSize(2);
    assertThat(resultado.hasMore()).isTrue();
    assertThat(resultado.lastComentarioId()).isEqualTo(BigInteger.TWO);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar comentários de post não encontrado")
  void deveLancarExcecaoAoBuscarComentariosDePostNaoEncontrado() {
    when(postRepository.existsById(BigInteger.valueOf(999))).thenReturn(false);

    assertThatThrownBy(() -> comentarioService.buscarComentariosDoPost(
      BigInteger.valueOf(999),
      null,
      20
    ))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Post não encontrado");
  }

  @Test
  @DisplayName("Deve buscar respostas do comentário com sucesso")
  void deveBuscarRespostasDoComentarioComSucesso() {
    List<ComentarioProjection> projections = List.of(
      createComentarioProjection(BigInteger.TWO),
      createComentarioProjection(BigInteger.valueOf(3))
    );

    when(comentarioRepository.existsById(BigInteger.ONE)).thenReturn(true);
    when(comentarioRepository.findRespostasByComentarioPaiId(eq(BigInteger.ONE), eq(null), eq(11)))
      .thenReturn(projections);

    ComentarioListResponseDTO resultado = comentarioService.buscarRespostasDoComentario(
      BigInteger.ONE,
      null,
      10
    );

    assertThat(resultado).isNotNull();
    assertThat(resultado.comentarios()).hasSize(2);
    assertThat(resultado.hasMore()).isFalse();

    verify(comentarioRepository, times(1))
      .findRespostasByComentarioPaiId(eq(BigInteger.ONE), eq(null), eq(11));
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar respostas de comentário não encontrado")
  void deveLancarExcecaoAoBuscarRespostasDeComentarioNaoEncontrado() {
    when(comentarioRepository.existsById(BigInteger.valueOf(999))).thenReturn(false);

    assertThatThrownBy(() -> comentarioService.buscarRespostasDoComentario(
      BigInteger.valueOf(999),
      null,
      10
    ))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário não encontrado");
  }

  @Test
  @DisplayName("Deve buscar comentários do usuário logado com sucesso")
  void deveBuscarComentariosDoUsuarioLogadoComSucesso() {
    List<ComentarioProjection> projections = List.of(
      createComentarioProjection(BigInteger.ONE),
      createComentarioProjection(BigInteger.TWO)
    );

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(comentarioRepository.findComentariosByUsuarioId(eq(BigInteger.ONE), eq(null), eq(21)))
      .thenReturn(projections);

    ComentarioListResponseDTO resultado = comentarioService.buscarComentariosDoUsuario(
      principal,
      null,
      20
    );

    assertThat(resultado).isNotNull();
    assertThat(resultado.comentarios()).hasSize(2);
    assertThat(resultado.hasMore()).isFalse();

    verify(comentarioRepository, times(1))
      .findComentariosByUsuarioId(eq(BigInteger.ONE), eq(null), eq(21));
  }

  @Test
  @DisplayName("Deve atualizar comentário com sucesso")
  void deveAtualizarComentarioComSucesso() {
    ComentarioUpdateDTO dto = new ComentarioUpdateDTO("Texto atualizado");

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(comentarioRepository.findById(BigInteger.TWO)).thenReturn(Optional.of(comentario));
    when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

    ComentarioResponseDTO resultado = comentarioService.atualizarComentario(
      BigInteger.TWO,
      dto,
      principal
    );

    assertThat(resultado).isNotNull();
    verify(comentarioRepository, times(1)).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar comentário não encontrado")
  void deveLancarExcecaoAoAtualizarComentarioNaoEncontrado() {
    ComentarioUpdateDTO dto = new ComentarioUpdateDTO("Texto atualizado");

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(comentarioRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> comentarioService.atualizarComentario(
      BigInteger.valueOf(999),
      dto,
      principal
    ))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário não encontrado");

    verify(comentarioRepository, never()).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar comentário de outro usuário")
  void deveLancarExcecaoAoAtualizarComentarioDeOutroUsuario() {
    Usuario outroUsuario = new Usuario();
    outroUsuario.setId(BigInteger.valueOf(999));
    outroUsuario.setEmail("outro@email.com");

    comentario.setUsuario(outroUsuario);

    ComentarioUpdateDTO dto = new ComentarioUpdateDTO("Texto atualizado");

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(comentarioRepository.findById(BigInteger.TWO)).thenReturn(Optional.of(comentario));

    assertThatThrownBy(() -> comentarioService.atualizarComentario(
      BigInteger.TWO,
      dto,
      principal
    ))
      .isInstanceOf(AccessDeniedException.class)
      .hasMessage("Você não tem permissão para editar este comentário");

    verify(comentarioRepository, never()).save(any(Comentario.class));
  }

  @Test
  @DisplayName("Deve deletar comentário com sucesso")
  void deveDeletarComentarioComSucesso() {
    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(comentarioRepository.findById(BigInteger.TWO)).thenReturn(Optional.of(comentario));

    comentarioService.deletarComentario(BigInteger.TWO, principal);

    verify(comentarioRepository, times(1)).deleteById(BigInteger.TWO);
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar comentário não encontrado")
  void deveLancarExcecaoAoDeletarComentarioNaoEncontrado() {
    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(comentarioRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> comentarioService.deletarComentario(
      BigInteger.valueOf(999),
      principal
    ))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Comentário não encontrado");

    verify(comentarioRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar comentário de outro usuário")
  void deveLancarExcecaoAoDeletarComentarioDeOutroUsuario() {
    Usuario outroUsuario = new Usuario();
    outroUsuario.setId(BigInteger.valueOf(999));
    outroUsuario.setEmail("outro@email.com");

    comentario.setUsuario(outroUsuario);

    when(principal.getName()).thenReturn("joao@email.com");
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(comentarioRepository.findById(BigInteger.TWO)).thenReturn(Optional.of(comentario));

    assertThatThrownBy(() -> comentarioService.deletarComentario(
      BigInteger.TWO,
      principal
    ))
      .isInstanceOf(AccessDeniedException.class)
      .hasMessage("Você não tem permissão para deletar este comentário");

    verify(comentarioRepository, never()).deleteById(any());
  }

  private ComentarioProjection createComentarioProjection(BigInteger id) {
    return new ComentarioProjection() {
      @Override
      public BigInteger getId() {
        return id;
      }

      @Override
      public BigInteger getPostId() {
        return BigInteger.ONE;
      }

      @Override
      public BigInteger getUsuarioId() {
        return BigInteger.ONE;
      }

      @Override
      public String getUsuarioNome() {
        return "João Silva";
      }

      @Override
      public String getTexto() {
        return "Texto do comentário";
      }

      @Override
      public Long getTotalUpVotes() {
        return 5L;
      }

      @Override
      public Long getTotalSuperVotes() {
        return 2L;
      }

      @Override
      public BigInteger getComentarioPaiId() {
        return null;
      }

      @Override
      public Timestamp getDataCriacao() {
        return Timestamp.from(Instant.now());
      }
    };
  }
}
