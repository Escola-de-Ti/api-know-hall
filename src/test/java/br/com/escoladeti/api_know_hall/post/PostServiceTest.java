package br.com.escoladeti.api_know_hall.post;

import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioResponseDTO;
import br.com.escoladeti.api_know_hall.dto.post.*;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.enums.*;
import br.com.escoladeti.api_know_hall.projection.comentario.ComentarioProjection;
import br.com.escoladeti.api_know_hall.projection.post.PostBuscaProjection;
import br.com.escoladeti.api_know_hall.projection.post.PostFeedProjection;
import br.com.escoladeti.api_know_hall.repository.ComentarioRepository;
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

  @Mock
  private ComentarioRepository comentarioRepository;

  @InjectMocks
  private PostService postService;

  private Usuario usuario;
  private Post post;
  private Tag tag;
  private PostCreateDTO postCreateDTO;

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
      "Título do Post",
      "Descrição do post",
      List.of(BigInteger.ONE)
    );
  }

  @Test
  @DisplayName("Deve criar post com sucesso")
  void deveCriarPostComSucesso() {
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(tagsRepository.findAllById(anyList())).thenReturn(List.of(tag));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDTO resultado = postService.criarPost(postCreateDTO, "joao@email.com");

    assertThat(resultado).isNotNull();
    assertThat(resultado.id()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.titulo()).isEqualTo("Título do Post");
    assertThat(resultado.usuarioId()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.tags()).hasSize(1);

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(tagsRepository).findAllById(anyList());
    verify(postRepository).save(any(Post.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar post com usuário inexistente")
  void deveLancarExcecaoAoCriarPostComUsuarioInexistente() {
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> postService.criarPost(postCreateDTO, "email@invalido.com"))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(usuarioRepository).findByEmail("email@invalido.com");
    verify(postRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve criar post sem tags")
  void deveCriarPostSemTags() {
    PostCreateDTO dtoSemTags = new PostCreateDTO(
      "Título",
      "Descrição",
      null
    );

    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.save(any(Post.class))).thenReturn(post);

    PostResponseDTO resultado = postService.criarPost(dtoSemTags, "joao@email.com");

    assertThat(resultado).isNotNull();
    verify(tagsRepository, never()).findAllById(anyList());
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar post com tag inexistente")
  void deveLancarExcecaoAoCriarPostComTagInexistente() {
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(tagsRepository.findAllById(anyList())).thenReturn(List.of(tag));

    PostCreateDTO dtoComTagsInvalidas = new PostCreateDTO(
      "Título",
      "Descrição",
      List.of(BigInteger.ONE, BigInteger.TWO)
    );

    assertThatThrownBy(() -> postService.criarPost(dtoComTagsInvalidas, "joao@email.com"))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Uma ou mais tags não foram encontradas");

    verify(postRepository, never()).save(any());
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

  @Test
  @DisplayName("Deve lançar exceção ao atualizar post com tag inexistente")
  void deveLancarExcecaoAoAtualizarPostComTagInexistente() {
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));
    when(tagsRepository.findAllById(anyList())).thenReturn(List.of(tag));

    PostUpdateDTO updateDTO = new PostUpdateDTO(
      null,
      null,
      List.of(BigInteger.ONE, BigInteger.TWO)
    );

    assertThatThrownBy(() -> postService.atualizarPost(BigInteger.ONE, updateDTO))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Uma ou mais tags não foram encontradas");

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

  // ==================== TESTES DE FEED (CORRIGIDOS) ====================

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
      null,
      OrderBy.RELEVANCE  // ✅ ADICIONADO
    );

    PostFeedProjection projection = createMockProjection();
    List<PostFeedProjection> projections = List.of(projection);

    // ✅ CORRIGIDO: Adicionado o 10º parâmetro (orderBy)
    when(postRepository.findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any()  // ✅ orderBy
    )).thenReturn(projections);

    lenient().when(postRepository.findById(any())).thenReturn(Optional.of(post));

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado).isNotNull();
    assertThat(resultado.posts()).hasSize(1);
    assertThat(resultado.hasMore()).isFalse();

    // ✅ CORRIGIDO: Verifica com 10 parâmetros
    verify(postRepository).findFeedPosts(anyLong(), any(), any(), anyInt(), any(), any(), any(), any(), any(), any());
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
      null,
      OrderBy.RELEVANCE  // ✅ ADICIONADO
    );

    // ✅ CORRIGIDO: Adicionado o 10º parâmetro (orderBy)
    when(postRepository.findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(),
      eq("1,2"),
      eq("AND"),
      eq(2),
      any(),
      any(),
      any()  // ✅ orderBy
    )).thenReturn(List.of());

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado.posts()).isEmpty();

    // ✅ CORRIGIDO: Verifica com 10 parâmetros
    verify(postRepository).findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(),
      eq("1,2"),
      eq("AND"),
      eq(2),
      any(),
      any(),
      any()  // ✅ orderBy
    );
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
      LocalDate.of(2025, 10, 22),
      OrderBy.RELEVANCE  // ✅ ADICIONADO
    );

    // ✅ CORRIGIDO: Adicionado o 10º parâmetro (orderBy)
    when(postRepository.findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(),
      any(),
      any(),
      any(),
      eq("2025-01-01"),
      eq("2025-10-22"),
      any()  // ✅ orderBy
    )).thenReturn(List.of());

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado.posts()).isEmpty();

    // ✅ CORRIGIDO: Verifica com 10 parâmetros
    verify(postRepository).findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(),
      any(),
      any(),
      any(),
      eq("2025-01-01"),
      eq("2025-10-22"),
      any()  // ✅ orderBy
    );
  }

  @Test
  @DisplayName("Deve indicar hasMore quando há mais posts")
  void deveIndicarHasMoreQuandoHaMaisPosts() {
    FeedRequestDTO request = new FeedRequestDTO(
      BigInteger.ONE,
      2,
      null,
      null,
      null,
      TagOperador.OR,
      null,
      null,
      OrderBy.RELEVANCE  // ✅ ADICIONADO
    );

    PostFeedProjection proj1 = createMockProjection();
    PostFeedProjection proj2 = createMockProjection();
    PostFeedProjection proj3 = createMockProjection();
    List<PostFeedProjection> projections = List.of(proj1, proj2, proj3);

    // ✅ CORRIGIDO: Adicionado o 10º parâmetro (orderBy)
    when(postRepository.findFeedPosts(
      anyLong(),
      any(),
      any(),
      anyInt(),
      any(),
      any(),
      any(),
      any(),
      any(),
      any()  // ✅ orderBy
    )).thenReturn(projections);

    lenient().when(postRepository.findById(any())).thenReturn(Optional.of(post));

    FeedResponseDTO resultado = postService.getFeed(request);

    assertThat(resultado.hasMore()).isTrue();
    assertThat(resultado.posts()).hasSize(2);

    // ✅ CORRIGIDO: Verifica com 10 parâmetros
    verify(postRepository).findFeedPosts(anyLong(), any(), any(), anyInt(), any(), any(), any(), any(), any(), any());
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
      null
    );

    PostBuscaProjection projection = createMockBuscaProjection();
    when(postRepository.buscarComFiltros(
      any(), eq("OR"), any(), any(), any(), eq("VOTOS"), eq("DESC"), any(), any(), anyInt(), any()
    )).thenReturn(List.of(projection));
    when(postRepository.findById(any())).thenReturn(Optional.of(post));

    PostBuscaResponseDTO resultado = postService.buscarPosts(request);

    assertThat(resultado).isNotNull();
    assertThat(resultado.posts()).hasSize(1);
    verify(postRepository).buscarComFiltros(
      any(), eq("OR"), any(), any(), any(), eq("VOTOS"), eq("DESC"), any(), any(), anyInt(), any()
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
      null
    );

    PostBuscaProjection projection = createMockBuscaProjection();
    when(postRepository.buscarComFiltros(
      any(), any(), any(), any(), any(), eq("DATA"), any(), any(), any(), anyInt(), any()
    )).thenReturn(List.of(projection));
    when(postRepository.findById(any())).thenReturn(Optional.of(post));

    PostBuscaResponseDTO resultado = postService.buscarPosts(request);

    assertThat(resultado.lastValue()).isNotNull();
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

      @Override
      public Boolean getJaVotou() {
        return false;
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
      "spring boot"
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
      "  spring boot  "
    );

    when(postRepository.buscarComFiltros(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), eq("spring boot")
    )).thenReturn(List.of());

    postService.buscarPosts(request);

    verify(postRepository).buscarComFiltros(
      any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), eq("spring boot")
    );
  }

  // ==================== TESTES DE DETALHES (CORRIGIDOS) ====================

  @Test
  @DisplayName("Deve buscar detalhes do post com sucesso")
  void deveBuscarDetalhesDoPostComSucesso() {
    ComentarioProjection comentario1 = createMockComentarioProjection(
      BigInteger.ONE, "Comentário 1", 5L, 2L
    );
    ComentarioProjection comentario2 = createMockComentarioProjection(
      BigInteger.TWO, "Comentário 2", 3L, 1L
    );

    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));

    when(comentarioRepository.findComentariosByPostId(
      eq(BigInteger.ONE),
      eq(null),
      eq(11),
      eq(usuario.getId())
    )).thenReturn(List.of(comentario1, comentario2));

    PostDetalhesDTO resultado = postService.buscarDetalhesDoPost(BigInteger.ONE, 10, "joao@email.com");

    assertThat(resultado).isNotNull();
    assertThat(resultado.id()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.titulo()).isEqualTo("Título do Post");
    assertThat(resultado.usuarioId()).isEqualTo(BigInteger.ONE);
    assertThat(resultado.usuarioNome()).isEqualTo("João Silva");
    assertThat(resultado.tags()).hasSize(1);
    assertThat(resultado.comentarios()).hasSize(2);
    assertThat(resultado.hasMoreComentarios()).isFalse();

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(postRepository).findById(BigInteger.ONE);
    verify(comentarioRepository).findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(11), eq(usuario.getId()));
  }

  @Test
  @DisplayName("Deve indicar hasMoreComentarios quando há mais comentários")
  void deveIndicarHasMoreComentariosQuandoHaMais() {
    List<ComentarioProjection> comentarios = new java.util.ArrayList<>();
    for (int i = 1; i <= 11; i++) {
      comentarios.add(createMockComentarioProjection(
        BigInteger.valueOf(i),
        "Comentário " + i,
        5L,
        2L
      ));
    }

    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));

    when(comentarioRepository.findComentariosByPostId(
      eq(BigInteger.ONE),
      eq(null),
      eq(11),
      eq(usuario.getId())
    )).thenReturn(comentarios);

    PostDetalhesDTO resultado = postService.buscarDetalhesDoPost(BigInteger.ONE, 10, "joao@email.com");

    assertThat(resultado).isNotNull();
    assertThat(resultado.comentarios()).hasSize(10);
    assertThat(resultado.hasMoreComentarios()).isTrue();

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(postRepository).findById(BigInteger.ONE);
    verify(comentarioRepository).findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(11), eq(usuario.getId()));
  }

  @Test
  @DisplayName("Deve buscar detalhes do post sem comentários")
  void deveBuscarDetalhesDoPostSemComentarios() {
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));

    when(comentarioRepository.findComentariosByPostId(
      eq(BigInteger.ONE),
      eq(null),
      eq(11),
      eq(usuario.getId())
    )).thenReturn(List.of());

    PostDetalhesDTO resultado = postService.buscarDetalhesDoPost(BigInteger.ONE, 10, "joao@email.com");

    assertThat(resultado).isNotNull();
    assertThat(resultado.comentarios()).isEmpty();
    assertThat(resultado.hasMoreComentarios()).isFalse();

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(postRepository).findById(BigInteger.ONE);
    verify(comentarioRepository).findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(11), eq(usuario.getId()));
  }

  @Test
  @DisplayName("Deve buscar detalhes do post sem tags")
  void deveBuscarDetalhesDoPostSemTags() {
    Post postSemTags = new Post();
    postSemTags.setId(BigInteger.ONE);
    postSemTags.setTitulo("Post sem tags");
    postSemTags.setDescricao("Descrição");
    postSemTags.setTotalUpVotes(5L);
    postSemTags.setUsuario(usuario);
    postSemTags.setDataCriacao(Timestamp.from(Instant.now()));
    postSemTags.setTags(List.of());

    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(postSemTags));

    when(comentarioRepository.findComentariosByPostId(
      eq(BigInteger.ONE),
      eq(null),
      eq(11),
      eq(usuario.getId())
    )).thenReturn(List.of());

    PostDetalhesDTO resultado = postService.buscarDetalhesDoPost(BigInteger.ONE, 10, "joao@email.com");

    assertThat(resultado).isNotNull();
    assertThat(resultado.tags()).isEmpty();
    assertThat(resultado.comentarios()).isEmpty();

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(postRepository).findById(BigInteger.ONE);
    verify(comentarioRepository).findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(11), eq(usuario.getId()));
  }

  @Test
  @DisplayName("Deve respeitar pageSize personalizado")
  void deveRespeitarPageSizePersonalizado() {
    List<ComentarioProjection> comentarios = new java.util.ArrayList<>();
    for (int i = 1; i <= 6; i++) {
      comentarios.add(createMockComentarioProjection(
        BigInteger.valueOf(i),
        "Comentário " + i,
        5L,
        2L
      ));
    }

    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));

    when(comentarioRepository.findComentariosByPostId(
      eq(BigInteger.ONE),
      eq(null),
      eq(6),
      eq(usuario.getId())
    )).thenReturn(comentarios);

    PostDetalhesDTO resultado = postService.buscarDetalhesDoPost(BigInteger.ONE, 5, "joao@email.com");

    assertThat(resultado.comentarios()).hasSize(5);
    assertThat(resultado.hasMoreComentarios()).isTrue();

    verify(usuarioRepository).findByEmail("joao@email.com");
    verify(comentarioRepository).findComentariosByPostId(eq(BigInteger.ONE), eq(null), eq(6), eq(usuario.getId()));
  }

  @Test
  @DisplayName("Deve mapear corretamente comentários para DTO")
  void deveMaperarCorretamenteComentariosParaDTO() {
    ComentarioProjection comentario = createMockComentarioProjection(
      BigInteger.ONE,
      "Texto do comentário",
      10L,
      3L
    );

    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
    when(postRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(post));

    when(comentarioRepository.findComentariosByPostId(
      eq(BigInteger.ONE),
      eq(null),
      eq(11),
      eq(usuario.getId())
    )).thenReturn(List.of(comentario));

    PostDetalhesDTO resultado = postService.buscarDetalhesDoPost(BigInteger.ONE, 10, "joao@email.com");

    assertThat(resultado.comentarios()).hasSize(1);
    ComentarioResponseDTO comentarioDTO = resultado.comentarios().get(0);
    assertThat(comentarioDTO.id()).isEqualTo(BigInteger.ONE);
    assertThat(comentarioDTO.texto()).isEqualTo("Texto do comentário");
    assertThat(comentarioDTO.totalUpVotes()).isEqualTo(10L);
    assertThat(comentarioDTO.totalSuperVotes()).isEqualTo(3L);
    assertThat(comentarioDTO.usuarioNome()).isEqualTo("João Silva");

    assertThat(comentarioDTO.jaVotou()).isFalse();
  }

  private ComentarioProjection createMockComentarioProjection(
    BigInteger id,
    String texto,
    Long upVotes,
    Long superVotes
  ) {
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
        return texto;
      }

      @Override
      public Long getTotalUpVotes() {
        return upVotes;
      }

      @Override
      public Long getTotalSuperVotes() {
        return superVotes;
      }

      @Override
      public BigInteger getComentarioPaiId() {
        return null;
      }

      public Boolean getJaVotou() {
        return false;
      }

      @Override
      public Timestamp getDataCriacao() {
        return Timestamp.from(Instant.now());
      }

      @Override
      public Integer getNivel() {
        return 1;
      }
    };
  }

  @Test
  @DisplayName("Deve buscar usuário por email com sucesso")
  void deveBuscarUsuarioPorEmailComSucesso() {
    when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

    Usuario resultado = postService.findUserByPrincipal("joao@email.com");

    assertThat(resultado).isNotNull();
    assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
    assertThat(resultado.getNome()).isEqualTo("João Silva");

    verify(usuarioRepository).findByEmail("joao@email.com");
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar usuário inexistente por email")
  void deveLancarExcecaoAoBuscarUsuarioInexistentePorEmail() {
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> postService.findUserByPrincipal("inexistente@email.com"))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(usuarioRepository).findByEmail("inexistente@email.com");
  }

  @Test
  @DisplayName("Deve adicionar imagem ao post com sucesso")
  void adicionaAtualizarImagemPost_sucesso() {
    BigInteger postId = BigInteger.ONE;
    Imagem imagem = new Imagem(postId, "img.png", "url", "idImg", "path", ImagemTipo.POST);
    Post post = new Post();
    post.setId(postId);
    post.setImagens(new java.util.ArrayList<>());
    when(postRepository.findById(postId)).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertThatCode(() -> postService.adicionaAtualizarImagemPost(imagem, 0, postId)).doesNotThrowAnyException();

    verify(postRepository).save(post);
    assertThat(post.getImagens()).isNotEmpty();
    assertThat(post.getImagens().get(0).getImagem()).isEqualTo(imagem);
  }

  @Test
  @DisplayName("Deve lançar exceção ao adicionar imagem em post inexistente")
  void adicionaAtualizarImagemPost_postNaoEncontrado_lancaExcecao() {
    BigInteger postId = BigInteger.TWO;
    Imagem imagem = new Imagem(postId, "img.png", "url", "idImg", "path", ImagemTipo.POST);
    when(postRepository.findById(postId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> postService.adicionaAtualizarImagemPost(imagem, 0, postId))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("Post não encontrado");
    verify(postRepository, never()).save(any(Post.class));
  }
}
