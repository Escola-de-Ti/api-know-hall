package br.com.escoladeti.api_know_hall.usuario;

import br.com.escoladeti.api_know_hall.controller.UsuarioController;
import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.config.JwtTokenService;
import br.com.escoladeti.api_know_hall.config.SecurityConfig;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioLoginDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.exception.UsuarioInativoException;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

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

  @MockBean
  private UsuarioService usuarioService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
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
    usuario.setCpf("12345678901");
    usuario.setNome("Test User");
    usuario.setSenhaHash("hashedPassword");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);

    usuarioCreateDTO = new UsuarioCreateDTO();
    usuarioCreateDTO.setEmail("test@test.com");
    usuarioCreateDTO.setCpf("12345678901");
    usuarioCreateDTO.setNome("Test User");
    usuarioCreateDTO.setSenha("hashedPassword");
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
      .andExpect(jsonPath("$[0].nome").value("Test User"));

    verify(usuarioService, times(1)).getAllUsuarios();
  }

  @Test
  void getUsuarioById_WithValidId_ShouldReturnUsuario() throws Exception {
    when(usuarioService.getUsuarioById(BigInteger.valueOf(1))).thenReturn(usuario);

    mockMvc.perform(get("/api/usuarios/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.email").value("test@test.com"))
      .andExpect(jsonPath("$.nome").value("Test User"));

    verify(usuarioService, times(1)).getUsuarioById(BigInteger.valueOf(1));
  }

  @Test
  void getUsuarioById_WithInvalidId_ShouldReturnNotFound() throws Exception {
    when(usuarioService.getUsuarioById(BigInteger.valueOf(999)))
      .thenThrow(new EntityNotFoundException("Usuario não encontrado"));

    mockMvc.perform(get("/api/usuarios/999"))
      .andExpect(status().isNotFound());

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
      .andExpect(jsonPath("$.email").value("test@test.com"))
      .andExpect(jsonPath("$.nome").value("Test User"));

    verify(usuarioService, times(1)).createUsuario(any(UsuarioCreateDTO.class));
  }

  @Test
  void updateUsuario_WithValidData_ShouldReturnUpdatedUsuario() throws Exception {
    when(usuarioService.updateUsuario(eq(BigInteger.valueOf(1)), any(UsuarioUpdateDTO.class))).thenReturn(usuario);

    mockMvc.perform(put("/api/usuarios/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.email").value("test@test.com"));

    verify(usuarioService, times(1)).updateUsuario(eq(BigInteger.valueOf(1)), any(UsuarioUpdateDTO.class));
  }

  @Test
  void updateUsuario_WithInvalidId_ShouldReturnNotFound() throws Exception {
    when(usuarioService.updateUsuario(eq(BigInteger.valueOf(999)), any(UsuarioUpdateDTO.class)))
      .thenThrow(new jakarta.persistence.EntityNotFoundException());

    mockMvc.perform(put("/api/usuarios/999")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
      .andExpect(status().isNotFound());

    verify(usuarioService, times(1)).updateUsuario(eq(BigInteger.valueOf(999)), any(UsuarioUpdateDTO.class));
  }

  @Test
  void updateUsuario_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    when(usuarioService.updateUsuario(eq(BigInteger.valueOf(1)), any(UsuarioUpdateDTO.class)))
      .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(put("/api/usuarios/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
      .andExpect(status().isInternalServerError());

    verify(usuarioService, times(1)).updateUsuario(eq(BigInteger.valueOf(1)), any(UsuarioUpdateDTO.class));
  }

  @Test
  void deleteUsuario_ShouldReturnNoContent() throws Exception {
    doNothing().when(usuarioService).deleteUsuario(BigInteger.valueOf(1));

    mockMvc.perform(delete("/api/usuarios/1"))
      .andExpect(status().isNoContent());

    verify(usuarioService, times(1)).deleteUsuario(BigInteger.valueOf(1));
  }

  @Test
  void deleteUsuario_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    doThrow(new RuntimeException("Database error")).when(usuarioService).deleteUsuario(BigInteger.valueOf(1));

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
    when(usuarioService.login("test@test.com", "senha")).thenReturn(new br.com.escoladeti.api_know_hall.dto.JwtTokenDTO("token", "Bearer", 3600L, "refreshToken"));

    UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("test@test.com", "senha");

    mockMvc.perform(post("/api/usuarios/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDTO)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.access_token").value("token"))
      .andExpect(jsonPath("$.token_type").value("Bearer"))
      .andExpect(jsonPath("$.expires_in").value(3600));

    verify(usuarioService, times(1)).login("test@test.com", "senha");
  }

  @Test
  void login_WithInvalidCredentials_ShouldReturnNotFound() throws Exception {
    when(usuarioService.login("naoexiste@test.com", "qualquer")).thenThrow(new EntityNotFoundException("Email ou senha inválidos"));

    UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("naoexiste@test.com", "qualquer");

    mockMvc.perform(post("/api/usuarios/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDTO)))
      .andExpect(status().isNotFound());

    verify(usuarioService, times(1)).login("naoexiste@test.com", "qualquer");
  }

  @Test
  void login_WithInactiveUser_ShouldReturnForbidden() throws Exception {
    when(usuarioService.login("test@test.com", "senha")).thenThrow(new UsuarioInativoException("Usuario inativo"));

    UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("test@test.com", "senha");

    mockMvc.perform(post("/api/usuarios/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDTO)))
      .andExpect(status().isForbidden());

    verify(usuarioService, times(1)).login("test@test.com", "senha");
  }

}
