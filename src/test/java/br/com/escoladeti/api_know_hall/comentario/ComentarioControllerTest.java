package br.com.escoladeti.api_know_hall.comentario;

import br.com.escoladeti.api_know_hall.controller.ComentarioController;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioListResponseDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioResponseDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUpdateDTO;
import br.com.escoladeti.api_know_hall.service.ComentarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = ComentarioController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
  },
  excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.escoladeti.api_know_hall.config.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.escoladeti.api_know_hall.security.*")
  }
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do ComentarioController")
class ComentarioControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ComentarioService comentarioService;

  private ComentarioResponseDTO comentarioResponse;
  private ComentarioCreateDTO comentarioCreate;
  private ComentarioUpdateDTO comentarioUpdate;
  private Principal mockPrincipal;

  @BeforeEach
  void setUp() {
    comentarioResponse = new ComentarioResponseDTO(
      BigInteger.ONE,
      BigInteger.ONE,
      BigInteger.ONE,
      "João Silva",
      "Comentário de teste",
      5L,
      2L,
      null,
      Timestamp.from(Instant.now())
    );

    comentarioCreate = new ComentarioCreateDTO(
      BigInteger.ONE,
      "Novo comentário",
      null
    );

    comentarioUpdate = new ComentarioUpdateDTO("Texto atualizado");

    mockPrincipal = () -> "joao@email.com";
  }

  @Test
  @DisplayName("Deve criar comentário com sucesso")
  void deveCriarComentarioComSucesso() throws Exception {
    when(comentarioService.criarComentario(any(ComentarioCreateDTO.class), any(Principal.class)))
      .thenReturn(comentarioResponse);

    mockMvc.perform(post("/api/comentarios")
        .principal(mockPrincipal)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(comentarioCreate)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.texto").value("Comentário de teste"))
      .andExpect(jsonPath("$.usuarioNome").value("João Silva"));

    verify(comentarioService, times(1))
      .criarComentario(any(ComentarioCreateDTO.class), any(Principal.class));
  }

  @Test
  @DisplayName("Deve retornar 400 ao criar comentário com dados inválidos")
  void deveRetornar400AoCriarComentarioComDadosInvalidos() throws Exception {
    ComentarioCreateDTO dtoInvalido = new ComentarioCreateDTO(null, "", null);

    mockMvc.perform(post("/api/comentarios")
        .principal(mockPrincipal)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dtoInvalido)))
      .andExpect(status().isBadRequest());

    verify(comentarioService, never())
      .criarComentario(any(ComentarioCreateDTO.class), any(Principal.class));
  }

  @Test
  @DisplayName("Deve retornar 404 ao criar comentário em post não encontrado")
  void deveRetornar404AoCriarComentarioEmPostNaoEncontrado() throws Exception {
    when(comentarioService.criarComentario(any(ComentarioCreateDTO.class), any(Principal.class)))
      .thenThrow(new EntityNotFoundException("Post não encontrado"));

    mockMvc.perform(post("/api/comentarios")
        .principal(mockPrincipal)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(comentarioCreate)))
      .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve buscar comentários do post com sucesso")
  void deveBuscarComentariosDoPostComSucesso() throws Exception {
    ComentarioListResponseDTO response = new ComentarioListResponseDTO(
      List.of(comentarioResponse),
      false,
      BigInteger.ONE
    );

    when(comentarioService.buscarComentariosDoPost(
      eq(BigInteger.ONE),
      eq(null),
      eq(20)
    )).thenReturn(response);

    mockMvc.perform(get("/api/comentarios/post/1")
        .param("pageSize", "20"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.comentarios").isArray())
      .andExpect(jsonPath("$.comentarios[0].id").value(1))
      .andExpect(jsonPath("$.hasMore").value(false));

    verify(comentarioService, times(1))
      .buscarComentariosDoPost(eq(BigInteger.ONE), eq(null), eq(20));
  }

  @Test
  @DisplayName("Deve buscar comentários do post com paginação")
  void deveBuscarComentariosDoPostComPaginacao() throws Exception {
    ComentarioListResponseDTO response = new ComentarioListResponseDTO(
      List.of(comentarioResponse),
      true,
      BigInteger.ONE
    );

    when(comentarioService.buscarComentariosDoPost(
      eq(BigInteger.ONE),
      eq(BigInteger.TEN),
      eq(10)
    )).thenReturn(response);

    mockMvc.perform(get("/api/comentarios/post/1")
        .param("lastComentarioId", "10")
        .param("pageSize", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.hasMore").value(true))
      .andExpect(jsonPath("$.lastComentarioId").value(1));

    verify(comentarioService, times(1))
      .buscarComentariosDoPost(eq(BigInteger.ONE), eq(BigInteger.TEN), eq(10));
  }

  @Test
  @DisplayName("Deve buscar respostas do comentário com sucesso")
  void deveBuscarRespostasDoComentarioComSucesso() throws Exception {
    ComentarioResponseDTO resposta = new ComentarioResponseDTO(
      BigInteger.TWO,
      BigInteger.ONE,
      BigInteger.ONE,
      "João Silva",
      "Resposta ao comentário",
      3L,
      1L,
      BigInteger.ONE,
      Timestamp.from(Instant.now())
    );

    ComentarioListResponseDTO response = new ComentarioListResponseDTO(
      List.of(resposta),
      false,
      BigInteger.TWO
    );

    when(comentarioService.buscarRespostasDoComentario(
      eq(BigInteger.ONE),
      eq(null),
      eq(10)
    )).thenReturn(response);

    mockMvc.perform(get("/api/comentarios/1/respostas")
        .param("pageSize", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.comentarios").isArray())
      .andExpect(jsonPath("$.comentarios[0].comentarioPaiId").value(1));

    verify(comentarioService, times(1))
      .buscarRespostasDoComentario(eq(BigInteger.ONE), eq(null), eq(10));
  }

  @Test
  @DisplayName("Deve buscar meus comentários com sucesso")
  void deveBuscarMeusComentariosComSucesso() throws Exception {
    ComentarioListResponseDTO response = new ComentarioListResponseDTO(
      List.of(comentarioResponse),
      false,
      BigInteger.ONE
    );

    when(comentarioService.buscarComentariosDoUsuario(
      any(Principal.class),
      eq(null),
      eq(20)
    )).thenReturn(response);

    mockMvc.perform(get("/api/comentarios/meus")
        .principal(mockPrincipal)
        .param("pageSize", "20"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.comentarios").isArray())
      .andExpect(jsonPath("$.comentarios[0].usuarioId").value(1));

    verify(comentarioService, times(1))
      .buscarComentariosDoUsuario(any(Principal.class), eq(null), eq(20));
  }

  @Test
  @DisplayName("Deve atualizar comentário com sucesso")
  void deveAtualizarComentarioComSucesso() throws Exception {
    ComentarioResponseDTO comentarioAtualizado = new ComentarioResponseDTO(
      BigInteger.ONE,
      BigInteger.ONE,
      BigInteger.ONE,
      "João Silva",
      "Texto atualizado",
      5L,
      2L,
      null,
      Timestamp.from(Instant.now())
    );

    when(comentarioService.atualizarComentario(
      eq(BigInteger.ONE),
      any(ComentarioUpdateDTO.class),
      any(Principal.class)
    )).thenReturn(comentarioAtualizado);

    mockMvc.perform(patch("/api/comentarios/1")
        .principal(mockPrincipal)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(comentarioUpdate)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.texto").value("Texto atualizado"));

    verify(comentarioService, times(1))
      .atualizarComentario(eq(BigInteger.ONE), any(ComentarioUpdateDTO.class), any(Principal.class));
  }

  @Test
  @DisplayName("Deve retornar 403 ao atualizar comentário de outro usuário")
  void deveRetornar403AoAtualizarComentarioDeOutroUsuario() throws Exception {
    when(comentarioService.atualizarComentario(
      eq(BigInteger.ONE),
      any(ComentarioUpdateDTO.class),
      any(Principal.class)
    )).thenThrow(new AccessDeniedException("Você não tem permissão para editar este comentário"));

    mockMvc.perform(patch("/api/comentarios/1")
        .principal(mockPrincipal)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(comentarioUpdate)))
      .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve deletar comentário com sucesso")
  void deveDeletarComentarioComSucesso() throws Exception {
    doNothing().when(comentarioService).deletarComentario(eq(BigInteger.ONE), any(Principal.class));

    mockMvc.perform(delete("/api/comentarios/1")
        .principal(mockPrincipal))
      .andExpect(status().isNoContent());

    verify(comentarioService, times(1))
      .deletarComentario(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @DisplayName("Deve retornar 404 ao deletar comentário não encontrado")
  void deveRetornar404AoDeletarComentarioNaoEncontrado() throws Exception {
    doThrow(new EntityNotFoundException("Comentário não encontrado"))
      .when(comentarioService).deletarComentario(eq(BigInteger.valueOf(999)), any(Principal.class));

    mockMvc.perform(delete("/api/comentarios/999")
        .principal(mockPrincipal))
      .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve retornar 403 ao deletar comentário de outro usuário")
  void deveRetornar403AoDeletarComentarioDeOutroUsuario() throws Exception {
    doThrow(new AccessDeniedException("Você não tem permissão para deletar este comentário"))
      .when(comentarioService).deletarComentario(eq(BigInteger.ONE), any(Principal.class));

    mockMvc.perform(delete("/api/comentarios/1")
        .principal(mockPrincipal))
      .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve buscar todos os comentários de um usuário com sucesso")
  void deveBuscarTodosComentariosDeUmUsuarioComSucesso() throws Exception {
    List<br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO> comentarios = List.of(
      new br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO(
        BigInteger.ONE,
        BigInteger.valueOf(10),
        "Primeiro comentário"
      ),
      new br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO(
        BigInteger.TWO,
        BigInteger.valueOf(20),
        "Segundo comentário"
      ),
      new br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO(
        BigInteger.valueOf(3),
        BigInteger.valueOf(30),
        "Terceiro comentário"
      )
    );

    when(comentarioService.buscarTodosComentariosDoUsuario(BigInteger.ONE))
      .thenReturn(comentarios);

    mockMvc.perform(get("/api/comentarios/usuario/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(3))
      .andExpect(jsonPath("$[0].comentarioId").value(1))
      .andExpect(jsonPath("$[0].postId").value(10))
      .andExpect(jsonPath("$[0].texto").value("Primeiro comentário"))
      .andExpect(jsonPath("$[1].comentarioId").value(2))
      .andExpect(jsonPath("$[1].postId").value(20))
      .andExpect(jsonPath("$[1].texto").value("Segundo comentário"))
      .andExpect(jsonPath("$[2].comentarioId").value(3))
      .andExpect(jsonPath("$[2].postId").value(30))
      .andExpect(jsonPath("$[2].texto").value("Terceiro comentário"));

    verify(comentarioService, times(1)).buscarTodosComentariosDoUsuario(BigInteger.ONE);
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando usuário não tem comentários")
  void deveRetornarListaVaziaQuandoUsuarioNaoTemComentarios() throws Exception {
    when(comentarioService.buscarTodosComentariosDoUsuario(BigInteger.valueOf(5)))
      .thenReturn(List.of());

    mockMvc.perform(get("/api/comentarios/usuario/5"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(0));

    verify(comentarioService, times(1)).buscarTodosComentariosDoUsuario(BigInteger.valueOf(5));
  }

  @Test
  @DisplayName("Deve retornar 404 quando usuário não existe")
  void deveRetornar404QuandoUsuarioNaoExiste() throws Exception {
    when(comentarioService.buscarTodosComentariosDoUsuario(BigInteger.valueOf(999)))
      .thenThrow(new EntityNotFoundException("Usuário não encontrado"));

    mockMvc.perform(get("/api/comentarios/usuario/999"))
      .andExpect(status().isNotFound());

    verify(comentarioService, times(1)).buscarTodosComentariosDoUsuario(BigInteger.valueOf(999));
  }

  @Test
  @DisplayName("Deve retornar comentários de diferentes posts")
  void deveRetornarComentariosDeDiferentesPosts() throws Exception {
    List<br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO> comentarios = List.of(
      new br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO(
        BigInteger.valueOf(10),
        BigInteger.ONE,
        "Comentário no post 1"
      ),
      new br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO(
        BigInteger.valueOf(11),
        BigInteger.TWO,
        "Comentário no post 2"
      ),
      new br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO(
        BigInteger.valueOf(12),
        BigInteger.ONE,
        "Outro comentário no post 1"
      )
    );

    when(comentarioService.buscarTodosComentariosDoUsuario(BigInteger.valueOf(3)))
      .thenReturn(comentarios);

    mockMvc.perform(get("/api/comentarios/usuario/3"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(3))
      .andExpect(jsonPath("$[0].postId").value(1))
      .andExpect(jsonPath("$[1].postId").value(2))
      .andExpect(jsonPath("$[2].postId").value(1));

    verify(comentarioService, times(1)).buscarTodosComentariosDoUsuario(BigInteger.valueOf(3));
  }

  @Test
  @DisplayName("Deve retornar apenas os campos necessários (comentarioId, postId, texto)")
  void deveRetornarApenasOsCamposNecessarios() throws Exception {
    List<br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO> comentarios = List.of(
      new br.com.escoladeti.api_know_hall.dto.comentario.ComentarioUsuarioResponseDTO(
        BigInteger.valueOf(100),
        BigInteger.valueOf(50),
        "Texto do comentário"
      )
    );

    when(comentarioService.buscarTodosComentariosDoUsuario(BigInteger.valueOf(7)))
      .thenReturn(comentarios);

    mockMvc.perform(get("/api/comentarios/usuario/7"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].comentarioId").value(100))
      .andExpect(jsonPath("$[0].postId").value(50))
      .andExpect(jsonPath("$[0].texto").value("Texto do comentário"))
      // Verificar que NÃO retorna outros campos
      .andExpect(jsonPath("$[0].usuarioId").doesNotExist())
      .andExpect(jsonPath("$[0].usuarioNome").doesNotExist())
      .andExpect(jsonPath("$[0].totalUpVotes").doesNotExist())
      .andExpect(jsonPath("$[0].dataCriacao").doesNotExist());

    verify(comentarioService, times(1)).buscarTodosComentariosDoUsuario(BigInteger.valueOf(7));
  }
}
