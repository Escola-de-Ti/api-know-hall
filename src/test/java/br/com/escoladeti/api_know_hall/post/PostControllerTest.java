package br.com.escoladeti.api_know_hall.post;

import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.config.SecurityConfig;
import br.com.escoladeti.api_know_hall.controller.PostController;
import br.com.escoladeti.api_know_hall.controller.TagsController;
import br.com.escoladeti.api_know_hall.dto.post.*;
import br.com.escoladeti.api_know_hall.dto.tags.TagResponseDTO;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoDirecao;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoTipo;
import br.com.escoladeti.api_know_hall.enums.TagOperador;
import br.com.escoladeti.api_know_hall.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;  // ✅ IMPORT EXPLÍCITO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;  // ✅ SEM any(), eq(), etc
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = PostController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
  },
  excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
  }
)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do PostController")
class PostControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private PostService postService;

  private PostResponseDTO postResponseDTO;
  private PostCreateDTO postCreateDTO;

  @BeforeEach
  void setUp() {
    TagResponseDTO tagDTO = new TagResponseDTO(BigInteger.ONE, "React Native");

    postResponseDTO = new PostResponseDTO(
      BigInteger.ONE,
      BigInteger.ONE,
      "João Silva",
      "Título do Post",
      "Descrição do post",
      10L,
      List.of(tagDTO),
      Timestamp.from(Instant.now())
    );

    postCreateDTO = new PostCreateDTO(
      BigInteger.ONE,
      "Título do Post",
      "Descrição do post",
      List.of(BigInteger.ONE)
    );
  }

  // ==================== TESTES DE CRIAÇÃO ====================

  @Test
  @WithMockUser
  @DisplayName("POST /api/posts - Deve criar post com sucesso")
  void deveCriarPostComSucesso() throws Exception {
    // ✅ USE ArgumentMatchers.any()
    when(postService.criarPost(ArgumentMatchers.any(PostCreateDTO.class))).thenReturn(postResponseDTO);

    mockMvc.perform(post("/api/posts")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(postCreateDTO)))
      .andDo(print())
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.titulo").value("Título do Post"))
      .andExpect(jsonPath("$.nomeUsuario").value("João Silva"));

    verify(postService, times(1)).criarPost(ArgumentMatchers.any(PostCreateDTO.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/posts - Deve retornar 400 com usuarioId nulo")
  void deveRetornar400ComUsuarioIdNulo() throws Exception {
    String jsonInvalido = """
      {
          "usuarioId": null,
          "titulo": "Título",
          "descricao": "Descrição"
      }
      """;

    mockMvc.perform(post("/api/posts")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonInvalido))
      .andDo(print())
      .andExpect(status().isBadRequest());

    verify(postService, never()).criarPost(ArgumentMatchers.any());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/posts - Deve retornar 400 com título vazio")
  void deveRetornar400ComTituloVazio() throws Exception {
    String jsonInvalido = """
      {
          "usuarioId": 1,
          "titulo": "",
          "descricao": "Descrição"
      }
      """;

    mockMvc.perform(post("/api/posts")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonInvalido))
      .andDo(print())
      .andExpect(status().isBadRequest());

    verify(postService, never()).criarPost(ArgumentMatchers.any());
  }

  // ==================== TESTES DE BUSCA ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/posts/{id} - Deve buscar post por ID")
  void deveBuscarPostPorId() throws Exception {
    when(postService.buscarPorId(BigInteger.ONE)).thenReturn(postResponseDTO);

    mockMvc.perform(get("/api/posts/1"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.titulo").value("Título do Post"));

    verify(postService).buscarPorId(BigInteger.ONE);
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/posts - Deve listar todos os posts")
  void deveListarTodosOsPosts() throws Exception {
    when(postService.listarTodos()).thenReturn(List.of(postResponseDTO));

    mockMvc.perform(get("/api/posts"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id").value(1));

    verify(postService).listarTodos();
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/posts/usuario/{usuarioId} - Deve listar posts por usuário")
  void deveListarPostsPorUsuario() throws Exception {
    when(postService.listarPorUsuario(BigInteger.ONE)).thenReturn(List.of(postResponseDTO));

    mockMvc.perform(get("/api/posts/usuario/1"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].usuarioId").value(1));

    verify(postService).listarPorUsuario(BigInteger.ONE);
  }

  // ==================== TESTES DE ATUALIZAÇÃO ====================

  @Test
  @WithMockUser
  @DisplayName("PATCH /api/posts/{id} - Deve atualizar post")
  void deveAtualizarPost() throws Exception {
    PostUpdateDTO updateDTO = new PostUpdateDTO("Novo Título", null, null);
    PostResponseDTO atualizado = new PostResponseDTO(
      BigInteger.ONE, BigInteger.ONE, "João Silva",
      "Novo Título", "Descrição", 10L, List.of(), Timestamp.from(Instant.now())
    );

    // ✅ USE ArgumentMatchers.eq() e .any()
    when(postService.atualizarPost(ArgumentMatchers.eq(BigInteger.ONE), ArgumentMatchers.any(PostUpdateDTO.class)))
      .thenReturn(atualizado);

    mockMvc.perform(patch("/api/posts/1")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDTO)))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.titulo").value("Novo Título"));

    verify(postService).atualizarPost(ArgumentMatchers.eq(BigInteger.ONE), ArgumentMatchers.any(PostUpdateDTO.class));
  }

  // ==================== TESTES DE DELEÇÃO ====================

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/posts/{id} - Deve deletar post")
  void deveDeletarPost() throws Exception {
    doNothing().when(postService).deletarPost(BigInteger.ONE);

    mockMvc.perform(delete("/api/posts/1")
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isNoContent());

    verify(postService).deletarPost(BigInteger.ONE);
  }

  // ==================== TESTES DE FEED ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/posts/feed - Deve buscar feed básico")
  void deveBuscarFeedBasico() throws Exception {
    PostFeedDTO feedDTO = new PostFeedDTO(
      BigInteger.ONE, BigInteger.ONE, "João", "Título", "Desc",
      10L, List.of(), Timestamp.from(Instant.now()), 50.0, 2
    );
    FeedResponseDTO feedResponse = new FeedResponseDTO(
      List.of(feedDTO), false, BigInteger.ONE, 50.0
    );

    when(postService.getFeed(ArgumentMatchers.any(FeedRequestDTO.class))).thenReturn(feedResponse);

    mockMvc.perform(get("/api/posts/feed")
        .param("usuarioId", "1")
        .param("pageSize", "10"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.posts", hasSize(1)))
      .andExpect(jsonPath("$.hasMore").value(false));

    verify(postService).getFeed(ArgumentMatchers.any(FeedRequestDTO.class));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/posts/feed - Deve buscar feed com filtros de tags")
  void deveBuscarFeedComFiltrosDeTags() throws Exception {
    when(postService.getFeed(ArgumentMatchers.any(FeedRequestDTO.class)))
      .thenReturn(new FeedResponseDTO(List.of(), false, null, null));

    mockMvc.perform(get("/api/posts/feed")
        .param("usuarioId", "1")
        .param("tagIds", "1,2")
        .param("tagOperador", "AND"))
      .andDo(print())
      .andExpect(status().isOk());

    verify(postService).getFeed(ArgumentMatchers.argThat(req ->
      req.tagIds() != null &&
        req.tagIds().size() == 2 &&
        req.tagOperador() == TagOperador.AND
    ));
  }

  // ==================== TESTES DE BUSCA AVANÇADA ====================

  @Test
  @WithMockUser
  @DisplayName("GET /api/posts/buscar - Deve buscar com ordenação por votos")
  void deveBuscarComOrdenacaoPorVotos() throws Exception {
    PostBuscaItemDTO itemDTO = new PostBuscaItemDTO(
      BigInteger.ONE, BigInteger.ONE, "João", "Título", "Desc",
      10L, List.of(), Timestamp.from(Instant.now())
    );
    PostBuscaResponseDTO response = new PostBuscaResponseDTO(
      List.of(itemDTO), false, BigInteger.ONE, 10L
    );

    when(postService.buscarPosts(ArgumentMatchers.any(PostBuscaRequestDTO.class))).thenReturn(response);

    mockMvc.perform(get("/api/posts/buscar")
        .param("ordenacao", "VOTOS")
        .param("direcao", "DESC"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.posts", hasSize(1)));

    verify(postService).buscarPosts(ArgumentMatchers.argThat(req ->
      req.ordenacao() == OrdenacaoTipo.VOTOS &&
        req.direcao() == OrdenacaoDirecao.DESC
    ));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/posts/buscar - Deve usar valores padrão")
  void deveUsarValoresPadraoQuandoNaoEspecificados() throws Exception {
    when(postService.buscarPosts(ArgumentMatchers.any(PostBuscaRequestDTO.class)))
      .thenReturn(new PostBuscaResponseDTO(List.of(), false, null, null));

    mockMvc.perform(get("/api/posts/buscar"))
      .andDo(print())
      .andExpect(status().isOk());

    verify(postService).buscarPosts(ArgumentMatchers.argThat(req ->
      req.ordenacao() == OrdenacaoTipo.DATA &&
        req.direcao() == OrdenacaoDirecao.DESC &&
        req.tagOperador() == TagOperador.OR &&
        req.pageSize() == 20
    ));
  }
}
