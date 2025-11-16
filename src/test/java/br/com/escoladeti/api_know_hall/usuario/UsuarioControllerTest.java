package br.com.escoladeti.api_know_hall.usuario;

import br.com.escoladeti.api_know_hall.controller.UsuarioController;
import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.config.JwtTokenService;
import br.com.escoladeti.api_know_hall.config.SecurityConfig;
import br.com.escoladeti.api_know_hall.dto.usuario.*;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.exception.DuplicateResourceException;
import br.com.escoladeti.api_know_hall.exception.InvalidCredentialsException;
import br.com.escoladeti.api_know_hall.exception.UsuarioInativoException;
import br.com.escoladeti.api_know_hall.exception.ValidationException;
import br.com.escoladeti.api_know_hall.exception.handler.GlobalExceptionHandler;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = UsuarioController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
  },
  excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
  }
)
@AutoConfigureWebMvc
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UsuarioControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UsuarioService usuarioService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean
  private JwtTokenService jwtTokenService;

  @Autowired
  private ObjectMapper objectMapper;

  private Usuario usuario;
  private UsuarioCreateDTO usuarioCreateDTO;
  private UsuarioUpdateDTO usuarioUpdateDTO;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(BigInteger.valueOf(1));
    usuario.setEmail("test@test.com");
    usuario.setCpf("12345678909");
    usuario.setNome("Test User");
    usuario.setBiografia("Desenvolvedor Java");
    usuario.setSenhaHash("$2a$12$hashedPassword");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario.setQntdToken(0L);
    usuario.setQntdXp(0L);

    usuarioCreateDTO = new UsuarioCreateDTO();
    usuarioCreateDTO.setEmail("test@test.com");
    usuarioCreateDTO.setCpf("12345678909");
    usuarioCreateDTO.setNome("Test User");
    usuarioCreateDTO.setBiografia("Desenvolvedor Java");
    usuarioCreateDTO.setSenha("Senha@123");
    usuarioCreateDTO.setTipoUsuario(TipoUsuario.ALUNO);

    usuarioUpdateDTO = new UsuarioUpdateDTO();
    usuarioUpdateDTO.setEmail("updated@test.com");
    usuarioUpdateDTO.setNome("Updated User");
  }

  @Test
  void getAllUsuarios_ShouldReturnListOfUsuarios() throws Exception {
    List<Usuario> usuarios = Arrays.asList(usuario);
    when(usuarioService.getAllUsuarios()).thenReturn(usuarios);

    mockMvc.perform(get("/api/usuarios"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].email").value("test@test.com"))
      .andExpect(jsonPath("$[0].nome").value("Test User"))
      .andExpect(jsonPath("$[0].tipoUsuario").value("ALUNO"))
      .andExpect(jsonPath("$[0].qntdToken").value(0))
      .andExpect(jsonPath("$[0].qntdXp").value(0))
      .andExpect(jsonPath("$[0].senhaHash").doesNotExist())  // Não deve expor senha
      .andExpect(jsonPath("$[0].cpf").doesNotExist());  // Não deve expor CPF

    verify(usuarioService, times(1)).getAllUsuarios();
  }

  @Test
  void getUsuarioById_WithValidId_ShouldReturnUsuario() throws Exception {
    when(usuarioService.getUsuarioById(BigInteger.valueOf(1))).thenReturn(usuario);

    mockMvc.perform(get("/api/usuarios/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.email").value("test@test.com"))
      .andExpect(jsonPath("$.nome").value("Test User"))
      .andExpect(jsonPath("$.biografia").value("Desenvolvedor Java"))
      .andExpect(jsonPath("$.tipoUsuario").value("ALUNO"))
      .andExpect(jsonPath("$.statusUsuario").value("ATIVO"))
      .andExpect(jsonPath("$.qntdToken").value(0))
      .andExpect(jsonPath("$.qntdXp").value(0))
      .andExpect(jsonPath("$.senhaHash").doesNotExist())  // Não deve expor senha
      .andExpect(jsonPath("$.cpf").doesNotExist());  // Não deve expor CPF

    verify(usuarioService, times(1)).getUsuarioById(BigInteger.valueOf(1));
  }

  @Test
  void getUsuarioById_WithInvalidId_ShouldReturnNotFound() throws Exception {
    when(usuarioService.getUsuarioById(BigInteger.valueOf(999)))
      .thenThrow(new EntityNotFoundException("Usuário não encontrado"));

    mockMvc.perform(get("/api/usuarios/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"));

    verify(usuarioService, times(1)).getUsuarioById(BigInteger.valueOf(999));
  }

  @Test
  void createUsuario_WithValidData_ShouldReturnCreatedUsuario() throws Exception {
    when(usuarioService.createUsuario(any(UsuarioCreateDTO.class))).thenReturn(usuario);

    mockMvc.perform(post("/api/usuarios")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.email").value("test@test.com"))
      .andExpect(jsonPath("$.nome").value("Test User"))
      .andExpect(jsonPath("$.biografia").value("Desenvolvedor Java"))
      .andExpect(jsonPath("$.tipoUsuario").value("ALUNO"))
      .andExpect(jsonPath("$.qntdToken").value(0))
      .andExpect(jsonPath("$.qntdXp").value(0))
      .andExpect(jsonPath("$.senhaHash").doesNotExist())
      .andExpect(jsonPath("$.cpf").doesNotExist());

    verify(usuarioService, times(1)).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void createUsuario_WithInvalidCPF_ShouldReturnBadRequest() throws Exception {
    when(usuarioService.createUsuario(any(UsuarioCreateDTO.class)))
      .thenThrow(new ValidationException("CPF inválido"));

    mockMvc.perform(post("/api/usuarios")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("CPF inválido"));

    verify(usuarioService, times(1)).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void createUsuario_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
    when(usuarioService.createUsuario(any(UsuarioCreateDTO.class)))
      .thenThrow(new DuplicateResourceException("Email já cadastrado no sistema"));

    mockMvc.perform(post("/api/usuarios")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(409))
      .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema"));

    verify(usuarioService, times(1)).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void createUsuario_WithWeakPassword_ShouldReturnBadRequest() throws Exception {
    when(usuarioService.createUsuario(any(UsuarioCreateDTO.class)))
      .thenThrow(new ValidationException("Senha deve ter no mínimo 8 caracteres"));

    mockMvc.perform(post("/api/usuarios")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Senha deve ter no mínimo 8 caracteres"));

    verify(usuarioService, times(1)).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void createUsuario_WithProhibitedWord_ShouldReturnBadRequest() throws Exception {
    when(usuarioService.createUsuario(any(UsuarioCreateDTO.class)))
      .thenThrow(new ValidationException("Nome contém conteúdo não permitido: IDIOTA"));

    mockMvc.perform(post("/api/usuarios")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Nome contém conteúdo não permitido: IDIOTA"));

    verify(usuarioService, times(1)).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void updateUsuario_WithValidData_ShouldReturnUpdatedUsuario() throws Exception {
    Usuario updatedUsuario = new Usuario();
    updatedUsuario.setId(BigInteger.valueOf(1));
    updatedUsuario.setEmail("updated@test.com");
    updatedUsuario.setNome("Updated User");
    updatedUsuario.setTipoUsuario(TipoUsuario.ALUNO);
    updatedUsuario.setStatusUsuario(StatusUsuario.ATIVO);
    updatedUsuario.setQntdToken(0L);
    updatedUsuario.setQntdXp(0L);

    when(usuarioService.updateUsuario(eq("email"), any(UsuarioUpdateDTO.class)))
      .thenReturn(updatedUsuario);

    mockMvc.perform(put("/api/usuarios/user")
        .principal(() -> "email")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.email").value("updated@test.com"))
      .andExpect(jsonPath("$.nome").value("Updated User"))
      .andExpect(jsonPath("$.senhaHash").doesNotExist())  // Não deve expor senha
      .andExpect(jsonPath("$.cpf").doesNotExist());  // Não deve expor CPF

    verify(usuarioService, times(1)).updateUsuario(eq("email"), any(UsuarioUpdateDTO.class));
  }

  @Test
  void updateUsuario_WithInvalidEmail_ShouldReturnNotFound() throws Exception {
    when(usuarioService.updateUsuario(eq("email"), any(UsuarioUpdateDTO.class)))
      .thenThrow(new EntityNotFoundException("Usuário não encontrado"));

    mockMvc.perform(put("/api/usuarios/user")
        .principal(() -> "email")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"));

    verify(usuarioService, times(1)).updateUsuario(eq("email"), any(UsuarioUpdateDTO.class));
  }

  @Test
  void updateUsuario_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
    when(usuarioService.updateUsuario(eq("email"), any(UsuarioUpdateDTO.class)))
      .thenThrow(new DuplicateResourceException("Email já cadastrado no sistema"));

    mockMvc.perform(put("/api/usuarios/user")
        .principal(() -> "email")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(409))
      .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema"));

    verify(usuarioService, times(1)).updateUsuario(eq("email"), any(UsuarioUpdateDTO.class));
  }

  @Test
  void updateUsuario_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    when(usuarioService.updateUsuario(eq("email"), any(UsuarioUpdateDTO.class)))
      .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(put("/api/usuarios/user")
        .principal(() -> "email")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
      .andExpect(status().isInternalServerError());

    verify(usuarioService, times(1)).updateUsuario(eq("email"), any(UsuarioUpdateDTO.class));
  }

  @Test
  void deleteUsuario_WithValidId_ShouldReturnNoContent() throws Exception {
    doNothing().when(usuarioService).deleteUsuario(BigInteger.valueOf(1));

    mockMvc.perform(delete("/api/usuarios/1"))
      .andExpect(status().isNoContent());

    verify(usuarioService, times(1)).deleteUsuario(BigInteger.valueOf(1));
  }

  @Test
  void deleteUsuario_WithInvalidId_ShouldReturnNotFound() throws Exception {
    doThrow(new EntityNotFoundException("Usuário não encontrado"))
      .when(usuarioService).deleteUsuario(BigInteger.valueOf(999));

    mockMvc.perform(delete("/api/usuarios/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"));

    verify(usuarioService, times(1)).deleteUsuario(BigInteger.valueOf(999));
  }

  @Test
  void deleteUsuario_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    doThrow(new RuntimeException("Database error"))
      .when(usuarioService).deleteUsuario(BigInteger.valueOf(1));

    mockMvc.perform(delete("/api/usuarios/1"))
      .andExpect(status().isInternalServerError());

    verify(usuarioService, times(1)).deleteUsuario(BigInteger.valueOf(1));
  }

  @Test
  void getAllUsuarios_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    when(usuarioService.getAllUsuarios()).thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(get("/api/usuarios"))
      .andExpect(status().isInternalServerError());

    verify(usuarioService, times(1)).getAllUsuarios();
  }

  @Test
  void login_WithValidCredentials_ShouldReturnToken() throws Exception {
    when(usuarioService.login("test@test.com", "Senha@123"))
      .thenReturn(new br.com.escoladeti.api_know_hall.dto.JwtTokenDTO("token", "Bearer", 3600L, "refreshToken"));

    UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("test@test.com", "Senha@123");

    mockMvc.perform(post("/api/usuarios/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDTO)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.access_token").value("token"))
      .andExpect(jsonPath("$.token_type").value("Bearer"))
      .andExpect(jsonPath("$.expires_in").value(3600))
      .andExpect(jsonPath("$.refresh_token").value("refreshToken"));

    verify(usuarioService, times(1)).login("test@test.com", "Senha@123");
  }

  @Test
  void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
    when(usuarioService.login("naoexiste@test.com", "qualquer"))
      .thenThrow(new InvalidCredentialsException("Email ou senha inválidos"));

    UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("naoexiste@test.com", "qualquer");

    mockMvc.perform(post("/api/usuarios/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDTO)))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.status").value(401))
      .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));

    verify(usuarioService, times(1)).login("naoexiste@test.com", "qualquer");
  }

  @Test
  void login_WithInactiveUser_ShouldReturnForbidden() throws Exception {
    when(usuarioService.login("test@test.com", "Senha@123"))
      .thenThrow(new UsuarioInativoException("Usuário inativo. Entre em contato com o suporte."));

    UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("test@test.com", "Senha@123");

    mockMvc.perform(post("/api/usuarios/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDTO)))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(403))
      .andExpect(jsonPath("$.message").value("Usuário inativo. Entre em contato com o suporte."));

    verify(usuarioService, times(1)).login("test@test.com", "Senha@123");
  }

  @Test
  void createUsuario_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
    UsuarioCreateDTO invalidDTO = new UsuarioCreateDTO();

    mockMvc.perform(post("/api/usuarios")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());

    verify(usuarioService, never()).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void createUsuario_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
    usuarioCreateDTO.setEmail("emailinvalido");

    mockMvc.perform(post("/api/usuarios")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(usuarioCreateDTO)));

    verify(usuarioService, never()).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void createUsuario_WithShortPassword_ShouldReturnBadRequest() throws Exception {
    usuarioCreateDTO.setSenha("123");

    mockMvc.perform(post("/api/usuarios")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(usuarioCreateDTO)));

    verify(usuarioService, never()).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void obterRanking_WithAuthenticatedUser_ShouldReturnRankingResponse() throws Exception {
    String email = "test@test.com";

    UsuarioRankingDTO usuario1 = new UsuarioRankingDTO(BigInteger.valueOf(1), 1L, "Usuario Top 1", 5000, 10);
    UsuarioRankingDTO usuario2 = new UsuarioRankingDTO(BigInteger.valueOf(1), 2L, "Usuario Top 2", 4500, 9);
    UsuarioRankingDTO usuario3 = new UsuarioRankingDTO(BigInteger.valueOf(1), 3L, "Usuario Top 3", 4000, 8);

    List<UsuarioRankingDTO> rankingList = Arrays.asList(usuario1, usuario2, usuario3);

    UsuarioLogadoRankingDTO usuarioLogado = new UsuarioLogadoRankingDTO(15L, 250);

    RankingResponseDTO rankingResponse = new RankingResponseDTO(rankingList, usuarioLogado);

    when(usuarioService.obterRanking(email)).thenReturn(rankingResponse);

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.rankingList").isArray())
      .andExpect(jsonPath("$.rankingList.length()").value(3))

      .andExpect(jsonPath("$.rankingList[0].posicao").value(1))
      .andExpect(jsonPath("$.rankingList[0].nome").value("Usuario Top 1"))
      .andExpect(jsonPath("$.rankingList[0].qntdXp").value(5000))
      .andExpect(jsonPath("$.rankingList[0].nivel").value(10))

      .andExpect(jsonPath("$.rankingList[1].posicao").value(2))
      .andExpect(jsonPath("$.rankingList[1].nome").value("Usuario Top 2"))
      .andExpect(jsonPath("$.rankingList[1].qntdXp").value(4500))
      .andExpect(jsonPath("$.rankingList[1].nivel").value(9))

      .andExpect(jsonPath("$.rankingList[2].posicao").value(3))
      .andExpect(jsonPath("$.rankingList[2].nome").value("Usuario Top 3"))
      .andExpect(jsonPath("$.rankingList[2].qntdXp").value(4000))
      .andExpect(jsonPath("$.rankingList[2].nivel").value(8))

      .andExpect(jsonPath("$.usuarioLogado").exists())
      .andExpect(jsonPath("$.usuarioLogado.posicao").value(15))
      .andExpect(jsonPath("$.usuarioLogado.xpRecebidoUltimos30Dias").value(250));

    verify(usuarioService, times(1)).obterRanking(email);
  }

  @Test
  void obterRanking_WithUserNotFound_ShouldReturnNotFound() throws Exception {
    String email = "naoexiste@test.com";

    when(usuarioService.obterRanking(email))
      .thenThrow(new EntityNotFoundException("Usuário não encontrado"));

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"));

    verify(usuarioService, times(1)).obterRanking(email);
  }

  @Test
  void obterRanking_WithEmptyRankingList_ShouldReturnEmptyList() throws Exception {
    String email = "test@test.com";

    List<UsuarioRankingDTO> rankingList = Arrays.asList();
    UsuarioLogadoRankingDTO usuarioLogado = new UsuarioLogadoRankingDTO(1L, 0);
    RankingResponseDTO rankingResponse = new RankingResponseDTO(rankingList, usuarioLogado);

    when(usuarioService.obterRanking(email)).thenReturn(rankingResponse);

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.rankingList").isArray())
      .andExpect(jsonPath("$.rankingList.length()").value(0))
      .andExpect(jsonPath("$.usuarioLogado.posicao").value(1))
      .andExpect(jsonPath("$.usuarioLogado.xpRecebidoUltimos30Dias").value(0));

    verify(usuarioService, times(1)).obterRanking(email);
  }

  @Test
  void obterRanking_WithUserInTop50_ShouldReturnCorrectRanking() throws Exception {
    String email = "test@test.com";

    RankingResponseDTO rankingResponse = getRankingResponseDTO();

    when(usuarioService.obterRanking(email)).thenReturn(rankingResponse);

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.rankingList.length()").value(5))
      .andExpect(jsonPath("$.rankingList[4].posicao").value(5))
      .andExpect(jsonPath("$.rankingList[4].nome").value("Test User"))
      .andExpect(jsonPath("$.usuarioLogado.posicao").value(5))
      .andExpect(jsonPath("$.usuarioLogado.xpRecebidoUltimos30Dias").value(500));

    verify(usuarioService, times(1)).obterRanking(email);
  }

  private static RankingResponseDTO getRankingResponseDTO() {
    UsuarioRankingDTO usuario1 = new UsuarioRankingDTO(BigInteger.valueOf(1), 1L, "Top 1", 10000, 20);
    UsuarioRankingDTO usuario2 = new UsuarioRankingDTO(BigInteger.valueOf(1), 2L, "Top 2", 9000, 18);
    UsuarioRankingDTO usuario3 = new UsuarioRankingDTO(BigInteger.valueOf(1), 3L, "Top 3", 8000, 17);
    UsuarioRankingDTO usuario4 = new UsuarioRankingDTO(BigInteger.valueOf(1), 4L, "Top 4", 7000, 15);
    UsuarioRankingDTO usuario5 = new UsuarioRankingDTO(BigInteger.valueOf(1), 5L, "Test User", 6000, 14);

    List<UsuarioRankingDTO> rankingList = Arrays.asList(usuario1, usuario2, usuario3, usuario4, usuario5);

    UsuarioLogadoRankingDTO usuarioLogado = new UsuarioLogadoRankingDTO(5L, 500);

    RankingResponseDTO rankingResponse = new RankingResponseDTO(rankingList, usuarioLogado);
    return rankingResponse;
  }

  @Test
  void obterRanking_WithUserOutsideTop50_ShouldReturnUserPositionAnyway() throws Exception {
    String email = "test@test.com";

    UsuarioRankingDTO usuario1 = new UsuarioRankingDTO(BigInteger.valueOf(1), 1L, "Top 1", 10000, 20);
    UsuarioRankingDTO usuario2 = new UsuarioRankingDTO(BigInteger.valueOf(1), 2L, "Top 2", 9000, 18);

    List<UsuarioRankingDTO> rankingList = Arrays.asList(usuario1, usuario2);

    UsuarioLogadoRankingDTO usuarioLogado = new UsuarioLogadoRankingDTO(127L, 150);

    RankingResponseDTO rankingResponse = new RankingResponseDTO(rankingList, usuarioLogado);

    when(usuarioService.obterRanking(email)).thenReturn(rankingResponse);

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.rankingList.length()").value(2))
      .andExpect(jsonPath("$.usuarioLogado.posicao").value(127))
      .andExpect(jsonPath("$.usuarioLogado.xpRecebidoUltimos30Dias").value(150));

    verify(usuarioService, times(1)).obterRanking(email);
  }

  @Test
  void obterRanking_WithNoXpInLast30Days_ShouldReturnZeroXp() throws Exception {
    String email = "test@test.com";

    UsuarioRankingDTO usuario1 = new UsuarioRankingDTO(BigInteger.valueOf(1), 1L, "Top 1", 5000, 10);
    List<UsuarioRankingDTO> rankingList = Arrays.asList(usuario1);

    UsuarioLogadoRankingDTO usuarioLogado = new UsuarioLogadoRankingDTO(50L, 0);

    RankingResponseDTO rankingResponse = new RankingResponseDTO(rankingList, usuarioLogado);

    when(usuarioService.obterRanking(email)).thenReturn(rankingResponse);

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.usuarioLogado.xpRecebidoUltimos30Dias").value(0));

    verify(usuarioService, times(1)).obterRanking(email);
  }

  @Test
  void obterRanking_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    String email = "test@test.com";

    when(usuarioService.obterRanking(email))
      .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isInternalServerError());

    verify(usuarioService, times(1)).obterRanking(email);
  }

  @Test
  void obterRanking_WithFullTop50_ShouldReturn50Users() throws Exception {
    String email = "test@test.com";

    List<UsuarioRankingDTO> rankingList = new java.util.ArrayList<>();
    for (int i = 1; i <= 50; i++) {
      rankingList.add(new UsuarioRankingDTO(
        BigInteger.valueOf(1),
        (long) i,
        "Usuario " + i,
        5000 - (i * 50),
        20 - (i / 5)
      ));
    }

    UsuarioLogadoRankingDTO usuarioLogado = new UsuarioLogadoRankingDTO(75L, 300);

    RankingResponseDTO rankingResponse = new RankingResponseDTO(rankingList, usuarioLogado);

    when(usuarioService.obterRanking(email)).thenReturn(rankingResponse);

    mockMvc.perform(get("/api/usuarios/ranking")
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.rankingList.length()").value(50))
      .andExpect(jsonPath("$.rankingList[0].posicao").value(1))
      .andExpect(jsonPath("$.rankingList[49].posicao").value(50))
      .andExpect(jsonPath("$.usuarioLogado.posicao").value(75));

    verify(usuarioService, times(1)).obterRanking(email);
  }

  @Test
  void getUsuarioDetalhes_ProprioUsuario_ShouldReturnTokens() throws Exception {
    String email = "test@test.com";
    BigInteger usuarioId = BigInteger.valueOf(1);

    UsuarioDetalhesResponseDTO detalhes = new UsuarioDetalhesResponseDTO();
    detalhes.setNome("Test User");
    detalhes.setTags(Arrays.asList());
    detalhes.setBiografia("Desenvolvedor Java");
    detalhes.setNivel(5);
    detalhes.setXp(1500L);
    detalhes.setTokens(300L); // Tokens devem ser retornados para o próprio usuário
    detalhes.setQtdPosts(10);
    detalhes.setQtdComentarios(25);
    detalhes.setQtdUpVotes(50);
    detalhes.setQtdSuperVotes(5);
    detalhes.setQtdWorkshops(2);
    detalhes.setImagemUrl("https://example.com/imagem.jpg");

    when(usuarioService.obterDetalhesUsuario(email, usuarioId)).thenReturn(detalhes);

    mockMvc.perform(get("/api/usuarios/detalhes/{usuarioId}", usuarioId)
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.nome").value("Test User"))
      .andExpect(jsonPath("$.biografia").value("Desenvolvedor Java"))
      .andExpect(jsonPath("$.nivel").value(5))
      .andExpect(jsonPath("$.xp").value(1500))
      .andExpect(jsonPath("$.tokens").value(300)) // Tokens retornados
      .andExpect(jsonPath("$.qtdPosts").value(10))
      .andExpect(jsonPath("$.qtdComentarios").value(25))
      .andExpect(jsonPath("$.qtdUpVotes").value(50))
      .andExpect(jsonPath("$.qtdSuperVotes").value(5))
      .andExpect(jsonPath("$.qtdWorkshops").value(2))
      .andExpect(jsonPath("$.imagemUrl").value("https://example.com/imagem.jpg"));

    verify(usuarioService, times(1)).obterDetalhesUsuario(email, usuarioId);
  }

  @Test
  void getUsuarioDetalhes_OutroUsuario_ShouldNotReturnTokens() throws Exception {
    String email = "logado@test.com";
    BigInteger usuarioId = BigInteger.valueOf(2);

    UsuarioDetalhesResponseDTO detalhes = new UsuarioDetalhesResponseDTO();
    detalhes.setNome("Outro User");
    detalhes.setTags(Arrays.asList());
    detalhes.setBiografia("Designer UX");
    detalhes.setNivel(3);
    detalhes.setXp(800L);
    detalhes.setTokens(null); // Tokens NÃO devem ser retornados para outro usuário
    detalhes.setQtdPosts(5);
    detalhes.setQtdComentarios(12);
    detalhes.setQtdUpVotes(20);
    detalhes.setQtdSuperVotes(2);
    detalhes.setQtdWorkshops(1);
    detalhes.setImagemUrl("https://example.com/outro.jpg");

    when(usuarioService.obterDetalhesUsuario(email, usuarioId)).thenReturn(detalhes);

    mockMvc.perform(get("/api/usuarios/detalhes/{usuarioId}", usuarioId)
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.nome").value("Outro User"))
      .andExpect(jsonPath("$.biografia").value("Designer UX"))
      .andExpect(jsonPath("$.nivel").value(3))
      .andExpect(jsonPath("$.xp").value(800))
      .andExpect(jsonPath("$.tokens").doesNotExist()) // Tokens não retornados
      .andExpect(jsonPath("$.qtdPosts").value(5))
      .andExpect(jsonPath("$.qtdComentarios").value(12))
      .andExpect(jsonPath("$.qtdUpVotes").value(20))
      .andExpect(jsonPath("$.qtdSuperVotes").value(2))
      .andExpect(jsonPath("$.qtdWorkshops").value(1))
      .andExpect(jsonPath("$.imagemUrl").value("https://example.com/outro.jpg"));

    verify(usuarioService, times(1)).obterDetalhesUsuario(email, usuarioId);
  }

  @Test
  void getUsuarioDetalhes_UsuarioNaoEncontrado_ShouldReturnNotFound() throws Exception {
    String email = "test@test.com";
    BigInteger usuarioId = BigInteger.valueOf(999);

    when(usuarioService.obterDetalhesUsuario(email, usuarioId))
      .thenThrow(new EntityNotFoundException("Usuário não encontrado"));

    mockMvc.perform(get("/api/usuarios/detalhes/{usuarioId}", usuarioId)
        .principal(() -> email))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"));

    verify(usuarioService, times(1)).obterDetalhesUsuario(email, usuarioId);
  }

  @Test
  void getUsuarioDetalhes_SemImagemPerfil_ShouldReturnNullImagemUrl() throws Exception {
    String email = "test@test.com";
    BigInteger usuarioId = BigInteger.valueOf(1);

    UsuarioDetalhesResponseDTO detalhes = new UsuarioDetalhesResponseDTO();
    detalhes.setNome("Test User");
    detalhes.setTags(Arrays.asList());
    detalhes.setBiografia(null);
    detalhes.setNivel(1);
    detalhes.setXp(0L);
    detalhes.setTokens(0L);
    detalhes.setQtdPosts(0);
    detalhes.setQtdComentarios(0);
    detalhes.setQtdUpVotes(0);
    detalhes.setQtdSuperVotes(0);
    detalhes.setQtdWorkshops(0);
    detalhes.setImagemUrl(null); // Sem imagem

    when(usuarioService.obterDetalhesUsuario(email, usuarioId)).thenReturn(detalhes);

    mockMvc.perform(get("/api/usuarios/detalhes/{usuarioId}", usuarioId)
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.nome").value("Test User"))
      .andExpect(jsonPath("$.imagemUrl").doesNotExist()) // Imagem null
      .andExpect(jsonPath("$.qtdPosts").value(0))
      .andExpect(jsonPath("$.qtdComentarios").value(0))
      .andExpect(jsonPath("$.qtdUpVotes").value(0))
      .andExpect(jsonPath("$.qtdSuperVotes").value(0))
      .andExpect(jsonPath("$.qtdWorkshops").value(0));

    verify(usuarioService, times(1)).obterDetalhesUsuario(email, usuarioId);
  }

  @Test
  void getUsuarioDetalhes_ComTodosContadoresPreenchidos_ShouldReturnAllData() throws Exception {
    String email = "test@test.com";
    BigInteger usuarioId = BigInteger.valueOf(1);

    UsuarioDetalhesResponseDTO detalhes = new UsuarioDetalhesResponseDTO();
    detalhes.setNome("Usuario Ativo");
    detalhes.setTags(Arrays.asList());
    detalhes.setBiografia("Entusiasta de tecnologia");
    detalhes.setNivel(10);
    detalhes.setXp(5000L);
    detalhes.setTokens(1000L);
    detalhes.setQtdPosts(50);
    detalhes.setQtdComentarios(120);
    detalhes.setQtdUpVotes(200);
    detalhes.setQtdSuperVotes(30);
    detalhes.setQtdWorkshops(8);
    detalhes.setImagemUrl("https://example.com/ativo.jpg");

    when(usuarioService.obterDetalhesUsuario(email, usuarioId)).thenReturn(detalhes);

    mockMvc.perform(get("/api/usuarios/detalhes/{usuarioId}", usuarioId)
        .principal(() -> email))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.nome").value("Usuario Ativo"))
      .andExpect(jsonPath("$.biografia").value("Entusiasta de tecnologia"))
      .andExpect(jsonPath("$.nivel").value(10))
      .andExpect(jsonPath("$.xp").value(5000))
      .andExpect(jsonPath("$.tokens").value(1000))
      .andExpect(jsonPath("$.qtdPosts").value(50))
      .andExpect(jsonPath("$.qtdComentarios").value(120))
      .andExpect(jsonPath("$.qtdUpVotes").value(200))
      .andExpect(jsonPath("$.qtdSuperVotes").value(30))
      .andExpect(jsonPath("$.qtdWorkshops").value(8))
      .andExpect(jsonPath("$.imagemUrl").value("https://example.com/ativo.jpg"));

    verify(usuarioService, times(1)).obterDetalhesUsuario(email, usuarioId);
  }

  @Test
  void getUsuarioDetalhes_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    String email = "test@test.com";
    BigInteger usuarioId = BigInteger.valueOf(1);

    when(usuarioService.obterDetalhesUsuario(email, usuarioId))
      .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(get("/api/usuarios/detalhes/{usuarioId}", usuarioId)
        .principal(() -> email))
      .andExpect(status().isInternalServerError());

    verify(usuarioService, times(1)).obterDetalhesUsuario(email, usuarioId);
  }

  @Test
  void buscarUsuariosPorNome_WithValidName_ShouldReturnUsuariosList() throws Exception {
    String nome = "Test";

    UsuarioRankingDTO usuario1 = new UsuarioRankingDTO(BigInteger.valueOf(1), 1L, "Test User 1", 5000, 10);
    UsuarioRankingDTO usuario2 = new UsuarioRankingDTO(BigInteger.valueOf(2), 5L, "Test User 2", 3000, 7);

    List<UsuarioRankingDTO> resultados = Arrays.asList(usuario1, usuario2);

    when(usuarioService.buscarUsuariosPorNome(nome)).thenReturn(resultados);

    mockMvc.perform(get("/api/usuarios/buscar")
        .param("nome", nome))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].posicao").value(1))
      .andExpect(jsonPath("$[0].nome").value("Test User 1"))
      .andExpect(jsonPath("$[0].qntdXp").value(5000))
      .andExpect(jsonPath("$[0].nivel").value(10))
      .andExpect(jsonPath("$[1].id").value(2))
      .andExpect(jsonPath("$[1].posicao").value(5))
      .andExpect(jsonPath("$[1].nome").value("Test User 2"))
      .andExpect(jsonPath("$[1].qntdXp").value(3000))
      .andExpect(jsonPath("$[1].nivel").value(7));

    verify(usuarioService, times(1)).buscarUsuariosPorNome(nome);
  }

  @Test
  void buscarUsuariosPorNome_WithNoResults_ShouldReturnEmptyList() throws Exception {
    String nome = "NomeInexistente";

    when(usuarioService.buscarUsuariosPorNome(nome)).thenReturn(Arrays.asList());

    mockMvc.perform(get("/api/usuarios/buscar")
        .param("nome", nome))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(0));

    verify(usuarioService, times(1)).buscarUsuariosPorNome(nome);
  }

  @Test
  void buscarUsuariosPorNome_WithEmptyString_ShouldReturnEmptyList() throws Exception {
    String nome = "";

    when(usuarioService.buscarUsuariosPorNome(nome)).thenReturn(Arrays.asList());

    mockMvc.perform(get("/api/usuarios/buscar")
        .param("nome", nome))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(0));

    verify(usuarioService, times(1)).buscarUsuariosPorNome(nome);
  }

  @Test
  void buscarUsuariosPorNome_WithPartialMatch_ShouldReturnMatchingUsers() throws Exception {
    String nome = "Mari";

    UsuarioRankingDTO usuario1 = new UsuarioRankingDTO(BigInteger.valueOf(10), 3L, "Maria Silva", 4200, 9);
    UsuarioRankingDTO usuario2 = new UsuarioRankingDTO(BigInteger.valueOf(15), 8L, "Mario Santos", 2800, 6);

    List<UsuarioRankingDTO> resultados = Arrays.asList(usuario1, usuario2);

    when(usuarioService.buscarUsuariosPorNome(nome)).thenReturn(resultados);

    mockMvc.perform(get("/api/usuarios/buscar")
        .param("nome", nome))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].nome").value("Maria Silva"))
      .andExpect(jsonPath("$[1].nome").value("Mario Santos"));

    verify(usuarioService, times(1)).buscarUsuariosPorNome(nome);
  }

  @Test
  void buscarUsuariosPorNome_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    String nome = "Test";

    when(usuarioService.buscarUsuariosPorNome(nome))
      .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(get("/api/usuarios/buscar")
        .param("nome", nome))
      .andExpect(status().isInternalServerError());

    verify(usuarioService, times(1)).buscarUsuariosPorNome(nome);
  }

  @Test
  void buscarUsuariosPorNome_WithSpecialCharacters_ShouldReturnResults() throws Exception {
    String nome = "José";

    UsuarioRankingDTO usuario = new UsuarioRankingDTO(BigInteger.valueOf(20), 12L, "José Oliveira", 1500, 5);

    when(usuarioService.buscarUsuariosPorNome(nome)).thenReturn(Arrays.asList(usuario));

    mockMvc.perform(get("/api/usuarios/buscar")
        .param("nome", nome))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].nome").value("José Oliveira"));

    verify(usuarioService, times(1)).buscarUsuariosPorNome(nome);
  }
}
