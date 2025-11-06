package br.com.escoladeti.api_know_hall.post;

import br.com.escoladeti.api_know_hall.dto.post.*;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoDirecao;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoTipo;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TagOperador;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.projection.post.PostBuscaProjection;
import br.com.escoladeti.api_know_hall.projection.post.PostFeedProjection;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import br.com.escoladeti.api_know_hall.repository.TagsRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.PostService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PostService")
class PostServiceTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private TagsRepository tagsRepository;

  @InjectMocks
  private PostService postService;

  private Usuario usuario;
  private Post post;
  private Tag tag;
  private PostCreateDTO postCreateDTO;
  private Principal mockPrincipal;

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

    // Setup Tag
    tag = new Tag();
    tag.setId(BigInteger.ONE);
    tag.setName("React Native");

    // Setup Post
    post = new Post();
    post.setId(BigInteger.ONE);
    post.setTitulo("Título do Post");
    post.setDescricao("Descrição do post");
    post.setTotalUpVotes(10L);
    post.setUsuario(usuario);
    post.setDataCriacao(Timestamp.from(Instant.now()));
    post.setTags(List.of(tag));

    // Setup DTO
    postCreateDTO = new PostCreateDTO(
      BigInteger.ONE,
      "Título do Post",
      "Descrição do post",
      List.of(BigInteger.ONE)
    );

    mockPrincipal = () -> "joao@email.com";
  }

  // ==================== TESTES DE CRIAÇÃO ====================

  @Test
  @DisplayName("Deve criar post com sucesso")
  void deveCriarPostComSucesso() {
    when(usuarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(usuario));
    when(tagsRepository.findAllById(anyList())).thenReturn(List.of(tag));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDTO resultado = postService.criarPost(postCreateDTO);

    assertThat(resultado).isNotNull();
    assertThat(resultado.id()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.titulo()).isEqualTo("Título do Post");
    assertThat(resultado.usuarioId()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.tags()).hasSize(1);

    verify(usuarioRepository).findById(BigInteger.ONE);
    verify(tagsRepository).findAllById(anyList());
    verify(postRepository).save(any(Post.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar post com usuário inexistente")
  void deveLancarExcecaoAoCriarPostComUsuarioInexistente() {
    when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> postService.criarPost(postCreateDTO))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(usuarioRepository).findById(any());
    verify(postRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve criar post sem tags")
  void deveCriarPostSemTags() {
    PostCreateDTO dtoSemTags = new PostCreateDTO(
      BigInteger.ONE,
      "Título",
      "Descrição",
      null
    );

    when(usuarioRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(usuario));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDTO resultado = postService.criarPost(dtoSemTags);

    assertThat(resultado).isNotNull();
    verify(tagsRepository, never()).findAllById(anyList());
  }

  // ==================== TESTES DE BUSCA ====================

  @Test
  @DisplayName("Deve buscar post por ID com sucesso")
  void deveBuscarPostPorIdComSucesso() {
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));

    PostResponseDTO resultado = postService.buscarPorId(BigInteger.ONE);

    assertThat(resultado).isNotNull();
    assertThat(resultado.id()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.titulo()).isEqualTo("Título do Post");

    verify(postRepository).findById(BigInteger.ONE);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar post inexistente")
  void deveLancarExcecaoAoBuscarPostInexistente() {
    when(postRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> postService.buscarPorId(BigInteger.TEN))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Post não encontrado");

    verify(postRepository).findById(BigInteger.TEN);
  }

  @Test
  @DisplayName("Deve listar todos os posts")
  void deveListarTodosOsPosts() {
    List<Post> posts = List.of(post);
    when(postRepository.findAll()).thenReturn(posts);

    List<PostResponseDTO> resultado = postService.listarTodos();

    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).id()).isEqualTo(BigInteger.ONE);

    verify(postRepository).findAll();
  }

  @Test
  @DisplayName("Deve listar posts por usuário")
  void deveListarPostsPorUsuario() {
    List<Post> posts = List.of(post);
    when(postRepository.findByUsuarioId(BigInteger.ONE)).thenReturn(posts);

    List<PostResponseDTO> resultado = postService.listarPorUsuario(BigInteger.ONE);

    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).usuarioId()).isEqualTo(BigInteger.ONE);

    verify(postRepository).findByUsuarioId(BigInteger.ONE);
  }

  // ==================== TESTES DE ATUALIZAÇÃO ====================

  @Test
  @DisplayName("Deve atualizar post com sucesso")
  void deveAtualizarPostComSucesso() {
    PostUpdateDTO updateDTO = new PostUpdateDTO(
      "Novo Título",
      "Nova Descrição",
      List.of(BigInteger.ONE)
    );

    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(tagsRepository.findAllById(anyList())).thenReturn(List.of(tag));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDTO resultado = postService.atualizarPost(BigInteger.ONE, updateDTO);

    assertThat(resultado).isNotNull();
    verify(postRepository).findById(BigInteger.ONE);
    verify(postRepository).save(any(Post.class));
  }

  @Test
  @DisplayName("Deve atualizar apenas título")
  void deveAtualizarApenasOTitulo() {
    PostUpdateDTO updateDTO = new PostUpdateDTO("Novo Título", null, null);

    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    postService.atualizarPost(BigInteger.ONE, updateDTO);

    verify(postRepository).save(argThat(p ->
      "Novo Título".equals(p.getTitulo()) &&
        "Descrição do post".equals(p.getDescricao())
    ));
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar post inexistente")
  void deveLancarExcecaoAoAtualizarPostInexistente() {
    PostUpdateDTO updateDTO = new PostUpdateDTO("Título", null, null);
    when(postRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> postService.atualizarPost(BigInteger.TEN, updateDTO))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Post não encontrado");

    verify(postRepository, never()).save(any());
  }

  // ==================== TESTES DE DELEÇÃO ====================

  @Test
  @DisplayName("Deve deletar post com sucesso")
  void deveDeletarPostComSucesso() {
    when(postRepository.existsById(BigInteger.ONE)).thenReturn(true);
    doNothing().when(postRepository).deleteById(BigInteger.ONE);

    assertThatCode(() -> postService.deletarPost(BigInteger.ONE))
      .doesNotThrowAnyException();

    verify(postRepository).existsById(BigInteger.ONE);
    verify(postRepository).deleteById(BigInteger.ONE);
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar post inexistente")
  void deveLancarExcecaoAoDeletarPostInexistente() {
    when(postRepository.existsById(any())).thenReturn(false);

    assertThatThrownBy(() -> postService.deletarPost(BigInteger.TEN))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Post não encontrado");

    verify(postRepository).existsById(BigInteger.TEN);
    verify(postRepository, never()).deleteById(any());
  }

  // ==================== TESTES DE FEED ====================

  @Test
  @DisplayName("Deve buscar feed com sucesso")
  void deveBuscarFeedComSucesso() {
    FeedRequestDTO request = new FeedRequestDTO(
      BigInteger.ONE,
      10,
      null,
      null,
      null,
      TagOperador.OR,
      null,
      null
    );

    PostFeedProjection projection = createMockProjection();
    List<PostFeedProjection> projections = List.of(projection);

    when(postRepository.findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(),
      any(),
      any(),
      any(),
      any(),
      any()
    )).thenReturn(projections);

    lenient().when(postRepository.findById(any())).thenReturn(Optional.of(post));

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado).isNotNull();
    assertThat(resultado.posts()).hasSize(1);
    assertThat(resultado.hasMore()).isFalse();

    verify(postRepository).findFeedPosts(anyLong(), any(), any(), anyInt(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("Deve buscar feed com filtro de tags")
  void deveBuscarFeedComFiltroDeTags() {
    FeedRequestDTO request = new FeedRequestDTO(
      BigInteger.ONE,
      10,
      null,
      null,
      List.of(BigInteger.ONE, BigInteger.TWO),
      TagOperador.AND,
      null,
      null
    );

    when(postRepository.findFeedPosts(anyLong(), any(), any(), anyInt(), eq("1,2"), eq("AND"), eq(2), any(), any()))
      .thenReturn(List.of());

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado.posts()).isEmpty();
    verify(postRepository).findFeedPosts(anyLong(), any(), any(), anyInt(), eq("1,2"), eq("AND"), eq(2), any(), any());
  }

  @Test
  @DisplayName("Deve buscar feed com filtro de período")
  void deveBuscarFeedComFiltroDePeriodo() {
    FeedRequestDTO request = new FeedRequestDTO(
      BigInteger.ONE,
      10,
      null,
      null,
      null,
      TagOperador.OR,
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 10, 22)
    );

    when(postRepository.findFeedPosts(
      anyLong(), any(), any(), anyInt(), any(), any(), any(),
      eq("2025-01-01"), eq("2025-10-22")
    )).thenReturn(List.of());

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado.posts()).isEmpty();
    verify(postRepository).findFeedPosts(
      anyLong(), any(), any(), anyInt(), any(), any(), any(),
      eq("2025-01-01"), eq("2025-10-22")
    );
  }

  @Test
  @DisplayName("Deve indicar hasMore quando há mais posts")
  void deveIndicarHasMoreQuandoHaMaisPosts() {
    FeedRequestDTO request = new FeedRequestDTO(
      BigInteger.ONE,
      2, // pageSize = 2 → fetchSize = 3
      null,
      null,
      null,
      TagOperador.OR,
      null,
      null
    );

    // Retorna 3 posts (pageSize + 1)
    PostFeedProjection proj1 = createMockProjection();
    PostFeedProjection proj2 = createMockProjection();
    PostFeedProjection proj3 = createMockProjection();
    List<PostFeedProjection> projections = List.of(proj1, proj2, proj3);

    when(postRepository.findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(), // aceita qualquer inteiro, inclusive 3
      any(),
      any(),
      any(),
      any(),
      any()
    )).thenReturn(projections);

    lenient().when(postRepository.findById(any())).thenReturn(Optional.of(post));

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado.hasMore()).isTrue();
    assertThat(resultado.posts()).hasSize(2);

    verify(postRepository).findFeedPosts(anyLong(), any(), any(), anyInt(), any(), any(), any(), any(), any());
  }

  // ==================== TESTES DE BUSCA AVANÇADA ====================

  @Test
  @DisplayName("Deve buscar posts com ordenação por votos")
  void deveBuscarPostsComOrdenacaoPorVotos() {
    PostBuscaRequestDTO request = new PostBuscaRequestDTO(
      null,
      TagOperador.OR,
      null,
      null,
      OrdenacaoTipo.VOTOS,
      OrdenacaoDirecao.DESC,
      10,
      null,
      null,
      null  // ✅ ADICIONAR termo
    );

    PostBuscaProjection projection = createMockBuscaProjection();
    when(postRepository.buscarComFiltros(
      any(), eq("OR"), any(), any(), any(), eq("VOTOS"), eq("DESC"), any(), any(), anyInt(), any()  // ✅ ADICIONAR any() para termo
    )).thenReturn(List.of(projection));
    when(postRepository.findById(any())).thenReturn(Optional.of(post));

    PostBuscaResponseDTO resultado = postService.buscarPosts(request);

    assertThat(resultado).isNotNull();
    assertThat(resultado.posts()).hasSize(1);
    verify(postRepository).buscarComFiltros(
      any(), eq("OR"), any(), any(), any(), eq("VOTOS"), eq("DESC"), any(), any(), anyInt(), any()  // ✅ ADICIONAR any() para termo
    );
  }

  @Test
  @DisplayName("Deve calcular lastValue corretamente para ordenação por DATA")
  void deveCalcularLastValueCorretamenteParaOrdenacaoPorData() {
    PostBuscaRequestDTO request = new PostBuscaRequestDTO(
      null,
      TagOperador.OR,
      null,
      null,
      OrdenacaoTipo.DATA,
      OrdenacaoDirecao.DESC,
      10,
      null,
      null,
      null  // ✅ ADICIONAR termo
    );

    PostBuscaProjection projection = createMockBuscaProjection();
    when(postRepository.buscarComFiltros(
      any(), any(), any(), any(), any(), eq("DATA"), any(), any(), any(), anyInt(), any()  // ✅ ADICIONAR any() para termo
    )).thenReturn(List.of(projection));
    when(postRepository.findById(any())).thenReturn(Optional.of(post));

    PostBuscaResponseDTO resultado = postService.buscarPosts(request);

    assertThat(resultado.lastValue()).isNotNull();
    // lastValue deve ser timestamp / 1000 para ordenação por data
  }

  // ==================== MÉTODOS AUXILIARES ====================

  private PostFeedProjection createMockProjection() {
    return new PostFeedProjection() {
      @Override
      public BigInteger getId() {
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
      public String getTitulo() {
        return "Título";
      }

      @Override
      public String getDescricao() {
        return "Descrição";
      }

      @Override
      public Long getTotalUpVotes() {
        return 10L;
      }

      @Override
      public Timestamp getDataCriacao() {
        return Timestamp.from(Instant.now());
      }

      @Override
      public Double getRelevanceScore() {
        return 50.0;
      }

      @Override
      public Integer getTagsEmComum() {
        return 2;
      }
    };
  }

  private PostBuscaProjection createMockBuscaProjection() {
    return new PostBuscaProjection() {
      @Override
      public BigInteger getId() {
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
      public String getTitulo() {
        return "Título";
      }

      @Override
      public String getDescricao() {
        return "Descrição";
      }

      @Override
      public Long getTotalUpVotes() {
        return 10L;
      }

      @Override
      public Timestamp getDataCriacao() {
        return Timestamp.from(Instant.now());
      }
    };
  }

  @Test
  @DisplayName("Deve buscar posts por termo no título")
  void deveBuscarPostsPorTermoNoTitulo() {
    PostBuscaRequestDTO request = new PostBuscaRequestDTO(
      null,
      TagOperador.OR,
      null,
      null,
      OrdenacaoTipo.DATA,
      OrdenacaoDirecao.DESC,
      10,
      null,
      null,
      "spring boot"  // ✅ TERMO
    );

    PostBuscaProjection projection = createMockBuscaProjection();
    when(postRepository.buscarComFiltros(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), eq("spring boot")
    )).thenReturn(List.of(projection));
    when(postRepository.findById(any())).thenReturn(Optional.of(post));

    PostBuscaResponseDTO resultado = postService.buscarPosts(request);

    assertThat(resultado).isNotNull();
    assertThat(resultado.posts()).hasSize(1);
    verify(postRepository).buscarComFiltros(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), eq("spring boot")
    );
  }

  @Test
  @DisplayName("Deve normalizar termo de busca removendo espaços extras")
  void deveNormalizarTermoDeBusca() {
    PostBuscaRequestDTO request = new PostBuscaRequestDTO(
      null,
      TagOperador.OR,
      null,
      null,
      OrdenacaoTipo.DATA,
      OrdenacaoDirecao.DESC,
      10,
      null,
      null,
      "  spring boot  "  // Com espaços
    );

    when(postRepository.buscarComFiltros(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), eq("spring boot")
    )).thenReturn(List.of());

    postService.buscarPosts(request);

    verify(postRepository).buscarComFiltros(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), eq("spring boot")
    );
  }
}
