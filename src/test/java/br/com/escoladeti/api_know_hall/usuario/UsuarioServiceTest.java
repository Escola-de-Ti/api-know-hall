package br.com.escoladeti.api_know_hall.usuario;

import br.com.escoladeti.api_know_hall.config.JwtTokenService;
import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.exception.UsuarioInativoException;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UsuarioServiceTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private JwtTokenService jwtTokenService;

  @InjectMocks
  private UsuarioService usuarioService;

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
  void getAllUsuarios_ShouldReturnListOfUsuarios() {
    List<Usuario> usuarios = Arrays.asList(usuario);
    when(usuarioRepository.findAll()).thenReturn(usuarios);

    List<Usuario> result = usuarioService.getAllUsuarios();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(usuario.getEmail(), result.get(0).getEmail());
    verify(usuarioRepository, times(1)).findAll();
  }

  @Test
  void getUsuarioById_WithValidId_ShouldReturnUsuario() {
    when(usuarioRepository.findById(BigInteger.valueOf(1))).thenReturn(Optional.of(usuario));

    Usuario result = usuarioService.getUsuarioById(BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(usuario.getEmail(), result.getEmail());
    verify(usuarioRepository, times(1)).findById(BigInteger.valueOf(1));
  }

  @Test
  void getUsuarioById_WithInvalidId_ShouldThrowEntityNotFoundException() {
    when(usuarioRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> usuarioService.getUsuarioById(BigInteger.valueOf(999))
    );

    assertEquals("Usuario não encontrado", exception.getMessage());
    verify(usuarioRepository, times(1)).findById(BigInteger.valueOf(999));
  }

  @Test
  void createUsuario_WithValidData_ShouldReturnCreatedUsuario() {
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    Usuario result = usuarioService.createUsuario(usuarioCreateDTO);

    assertNotNull(result);
    assertEquals(usuario.getEmail(), result.getEmail());
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
  }

  @Test
  void updateUsuario_WithValidData_ShouldReturnUpdatedUsuario() {
    when(usuarioRepository.findById(BigInteger.valueOf(1))).thenReturn(Optional.of(usuario));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    Usuario result = usuarioService.updateUsuario(BigInteger.valueOf(1), usuarioUpdateDTO);

    assertNotNull(result);
    verify(usuarioRepository, times(1)).findById(BigInteger.valueOf(1));
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
  }

  @Test
  void updateUsuario_WithInvalidId_ShouldThrowException() {
    when(usuarioRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> usuarioService.updateUsuario(BigInteger.valueOf(999), usuarioUpdateDTO));

    verify(usuarioRepository, times(1)).findById(BigInteger.valueOf(999));
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void deleteUsuario_ShouldCallRepositoryDelete() {
    doNothing().when(usuarioRepository).deleteById(BigInteger.valueOf(1));

    usuarioService.deleteUsuario(BigInteger.valueOf(1));

    verify(usuarioRepository, times(1)).deleteById(BigInteger.valueOf(1));
  }

  @Test
  void login_WithValidCredentialsAndActiveUser_ShouldReturnJwtTokenDTO() {
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    when(jwtTokenService.generateTokenWithExpiration(usuario.getEmail())).thenReturn(new JwtTokenDTO("token", "Bearer", 3600L));

    JwtTokenDTO result = usuarioService.login(usuario.getEmail(), "senhaCorreta");
    assertNotNull(result);
    assertEquals("token", result.access_token());
    assertEquals("Bearer", result.token_type());
    assertEquals(3600L, result.expires_in());
    verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
    verify(jwtTokenService, times(1)).generateTokenWithExpiration(usuario.getEmail());
  }

  @Test
  void login_WithInvalidEmail_ShouldThrowEntityNotFoundException() {
    when(usuarioRepository.findByEmail("invalido@test.com")).thenReturn(Optional.empty());
    EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> usuarioService.login("invalido@test.com", "qualquerSenha"));
    assertEquals("Email ou senha inválidos", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail("invalido@test.com");
  }

  @Test
  void login_WithInactiveUser_ShouldThrowUsuarioInativoException() {
    usuario.setStatusUsuario(StatusUsuario.INATIVO);
    when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    UsuarioInativoException exception = assertThrows(UsuarioInativoException.class, () -> usuarioService.login(usuario.getEmail(), "senhaCorreta"));
    assertEquals("Usuario inativo", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
  }
}
