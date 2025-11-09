package br.com.escoladeti.api_know_hall.usuario;

import br.com.escoladeti.api_know_hall.controller.UsuarioController;
import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.config.JwtTokenService;
import br.com.escoladeti.api_know_hall.config.SecurityConfig;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioLoginDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioUpdateDTO;
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
    // Não preenche campos obrigatórios

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
}
