package br.com.escoladeti.api_know_hall.usuario;

import br.com.escoladeti.api_know_hall.config.JwtTokenService;
import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.exception.DuplicateResourceException;
import br.com.escoladeti.api_know_hall.exception.InvalidCredentialsException;
import br.com.escoladeti.api_know_hall.exception.UsuarioInativoException;
import br.com.escoladeti.api_know_hall.exception.ValidationException;
import br.com.escoladeti.api_know_hall.projection.usuario.UsuarioRankingProjection;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import br.com.escoladeti.api_know_hall.service.utils.PalavrasProibidasService;
import br.com.escoladeti.api_know_hall.util.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import br.com.escoladeti.api_know_hall.dto.usuario.RankingResponseDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioRankingDTO;

import static org.mockito.Mockito.mock;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UsuarioServiceTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private JwtTokenService jwtTokenService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private CpfValidator cpfValidator;

  @Mock
  private EmailValidator emailValidator;

  @Mock
  private TelefoneValidator telefoneValidator;

  @Mock
  private NomeValidator nomeValidator;

  @Mock
  private SenhaValidator senhaValidator;

  @Mock
  private PalavrasProibidasService palavrasProibidasService;

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
    usuario.setCpf("12345678909");
    usuario.setNome("Test User");
    usuario.setSenhaHash("$2a$12$hashedPassword");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario.setQntdToken(0L);
    usuario.setQntdXp(0L);
    usuario.setNivel(1);

    usuarioCreateDTO = new UsuarioCreateDTO();
    usuarioCreateDTO.setEmail("test@test.com");
    usuarioCreateDTO.setCpf("12345678909");
    usuarioCreateDTO.setNome("Test User");
    usuarioCreateDTO.setSenha("Senha@123");
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

    assertEquals("Usuário não encontrado", exception.getMessage());
    verify(usuarioRepository, times(1)).findById(BigInteger.valueOf(999));
  }

  @Test
  void createUsuario_WithValidData_ShouldReturnCreatedUsuario() {
    // Configurar mocks para validações passarem
    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(senhaValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(false);
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(usuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPassword");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    Usuario result = usuarioService.createUsuario(usuarioCreateDTO);

    assertNotNull(result);
    assertEquals(usuario.getEmail(), result.getEmail());
    verify(cpfValidator, times(1)).isValid(anyString());
    verify(emailValidator, times(1)).isValid(anyString());
    verify(nomeValidator, times(1)).isValid(anyString());
    verify(senhaValidator, times(1)).isValid(anyString());
    verify(passwordEncoder, times(1)).encode(anyString());
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
  }

  @Test
  void createUsuario_WithInvalidCPF_ShouldThrowValidationException() {
    when(cpfValidator.isValid(anyString())).thenReturn(false);

    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertEquals("CPF inválido", exception.getMessage());
    verify(cpfValidator, times(1)).isValid(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void createUsuario_WithInvalidEmail_ShouldThrowValidationException() {
    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(false);

    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertEquals("Formato de email inválido", exception.getMessage());
    verify(emailValidator, times(1)).isValid(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void createUsuario_WithDuplicateEmail_ShouldThrowDuplicateResourceException() {
    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(false);
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

    DuplicateResourceException exception = assertThrows(
      DuplicateResourceException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertEquals("Email já cadastrado no sistema", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void createUsuario_WithDuplicateCPF_ShouldThrowDuplicateResourceException() {
    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(false);
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(usuarioRepository.findByCpf(anyString())).thenReturn(Optional.of(usuario));

    DuplicateResourceException exception = assertThrows(
      DuplicateResourceException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertEquals("CPF já cadastrado no sistema", exception.getMessage());
    verify(usuarioRepository, times(1)).findByCpf(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void createUsuario_WithProhibitedWordInName_ShouldThrowValidationException() {
    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(true);
    when(palavrasProibidasService.identificarPalavraProibida(anyString())).thenReturn("IDIOTA");

    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertTrue(exception.getMessage().contains("Nome contém conteúdo não permitido"));
    verify(palavrasProibidasService, times(1)).contemPalavraProibida(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void createUsuario_WithWeakPassword_ShouldThrowValidationException() {
    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(false);
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(usuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
    when(senhaValidator.isValid(anyString())).thenReturn(false);
    when(senhaValidator.getMensagemErro(anyString())).thenReturn("Senha deve ter no mínimo 8 caracteres");

    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertEquals("Senha deve ter no mínimo 8 caracteres", exception.getMessage());
    verify(senhaValidator, times(1)).isValid(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void updateUsuario_WithValidData_ShouldReturnUpdatedUsuario() {
    when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(false);
    when(usuarioRepository.findByEmail("updated@test.com")).thenReturn(Optional.empty());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    Usuario result = usuarioService.updateUsuario("test@test.com", usuarioUpdateDTO);

    assertNotNull(result);
    verify(usuarioRepository, times(1)).findByEmail("test@test.com");
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
  }

  @Test
  void updateUsuario_WithInvalidEmail_ShouldThrowEntityNotFoundException() {
    when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> usuarioService.updateUsuario("test@test.com", usuarioUpdateDTO)
    );

    assertEquals("Usuário não encontrado", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail("test@test.com");
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void deleteUsuario_WithValidId_ShouldCallRepositoryDelete() {
    when(usuarioRepository.existsById(BigInteger.valueOf(1))).thenReturn(true);
    doNothing().when(usuarioRepository).deleteById(BigInteger.valueOf(1));

    usuarioService.deleteUsuario(BigInteger.valueOf(1));

    verify(usuarioRepository, times(1)).existsById(BigInteger.valueOf(1));
    verify(usuarioRepository, times(1)).deleteById(BigInteger.valueOf(1));
  }

  @Test
  void deleteUsuario_WithInvalidId_ShouldThrowEntityNotFoundException() {
    when(usuarioRepository.existsById(BigInteger.valueOf(999))).thenReturn(false);

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> usuarioService.deleteUsuario(BigInteger.valueOf(999))
    );

    assertEquals("Usuário não encontrado", exception.getMessage());
    verify(usuarioRepository, times(1)).existsById(BigInteger.valueOf(999));
    verify(usuarioRepository, never()).deleteById(any());
  }

  @Test
  void login_WithValidCredentialsAndActiveUser_ShouldReturnJwtTokenDTO() {
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(jwtTokenService.generateTokenWithExpiration(usuario.getEmail()))
      .thenReturn(new JwtTokenDTO("token", "Bearer", 3600L, "refreshToken"));

    JwtTokenDTO result = usuarioService.login(usuario.getEmail(), "Senha@123");

    assertNotNull(result);
    assertEquals("token", result.access_token());
    assertEquals("Bearer", result.token_type());
    assertEquals(3600L, result.expires_in());
    verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(jwtTokenService, times(1)).generateTokenWithExpiration(usuario.getEmail());
  }

  @Test
  void login_WithInvalidEmail_ShouldThrowInvalidCredentialsException() {
    when(usuarioRepository.findByEmail("invalido@test.com")).thenReturn(Optional.empty());

    InvalidCredentialsException exception = assertThrows(
      InvalidCredentialsException.class,
      () -> usuarioService.login("invalido@test.com", "qualquerSenha")
    );

    assertEquals("Email ou senha inválidos", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail("invalido@test.com");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  void login_WithInvalidPassword_ShouldThrowInvalidCredentialsException() {
    when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    InvalidCredentialsException exception = assertThrows(
      InvalidCredentialsException.class,
      () -> usuarioService.login(usuario.getEmail(), "senhaErrada")
    );

    assertEquals("Email ou senha inválidos", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(jwtTokenService, never()).generateTokenWithExpiration(anyString());
  }

  @Test
  void login_WithInactiveUser_ShouldThrowUsuarioInativoException() {
    usuario.setStatusUsuario(StatusUsuario.INATIVO);
    when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    UsuarioInativoException exception = assertThrows(
      UsuarioInativoException.class,
      () -> usuarioService.login(usuario.getEmail(), "Senha@123")
    );

    assertEquals("Usuário inativo. Entre em contato com o suporte.", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(jwtTokenService, never()).generateTokenWithExpiration(anyString());
  }

  @Test
  void refreshToken_ShouldReturnNewTokens() {
    String refreshToken = "validRefreshToken";
    JwtTokenDTO expectedToken = new JwtTokenDTO("newAccessToken", "Bearer", 3600L, "newRefreshToken");
    when(jwtTokenService.refreshTokens(refreshToken)).thenReturn(expectedToken);

    JwtTokenDTO result = usuarioService.refreshToken(refreshToken);

    assertNotNull(result);
    assertEquals("newAccessToken", result.access_token());
    verify(jwtTokenService, times(1)).refreshTokens(refreshToken);
  }

  @Test
  void atualizarImagemPerfil_WithValidData_ShouldUpdateImage() {
    Imagem imagem = new Imagem();
    imagem.setId(BigInteger.valueOf(1));

    when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    usuarioService.atualizarImagemPerfil("test@test.com", imagem);

    verify(usuarioRepository, times(1)).findByEmail("test@test.com");
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
    assertEquals(imagem, usuario.getImagemPerfil());
  }

  @Test
  void atualizarImagemPerfil_WithInvalidEmail_ShouldThrowEntityNotFoundException() {
    Imagem imagem = new Imagem();
    imagem.setId(BigInteger.valueOf(1));

    when(usuarioRepository.findByEmail("invalid@test.com")).thenReturn(Optional.empty());

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> usuarioService.atualizarImagemPerfil("invalid@test.com", imagem)
    );

    assertEquals("Usuário não encontrado", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail("invalid@test.com");
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void atualizarImagemPerfil_WithNullImage_ShouldSetNullImage() {
    when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    usuarioService.atualizarImagemPerfil("test@test.com", null);

    verify(usuarioRepository, times(1)).findByEmail("test@test.com");
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
    assertNull(usuario.getImagemPerfil());
  }

  @Test
  void createUsuario_WithValidTelefone_ShouldFormatTelefone() {
    usuarioCreateDTO.setTelefone("(11) 98765-4321");

    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(senhaValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(false);
    when(telefoneValidator.isValid(anyString())).thenReturn(true);
    when(telefoneValidator.formatarTelefone(anyString())).thenReturn("11987654321");
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(usuarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedPassword");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    Usuario result = usuarioService.createUsuario(usuarioCreateDTO);

    assertNotNull(result);
    verify(telefoneValidator, times(1)).formatarTelefone(anyString());
  }

  @Test
  void createUsuario_WithInvalidTelefone_ShouldThrowValidationException() {
    usuarioCreateDTO.setTelefone("123");

    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(anyString())).thenReturn(false);
    when(telefoneValidator.isValid(anyString())).thenReturn(false);

    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertEquals("Formato de telefone inválido", exception.getMessage());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void updateUsuario_WithNewPassword_ShouldEncryptPassword() {
    usuarioUpdateDTO.setSenha("NovaSenha@123");
    usuarioUpdateDTO.setEmail(null); // Remove email do update para não validar
    usuarioUpdateDTO.setNome(null);   // Remove nome do update para não validar

    when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
    when(senhaValidator.isValid(anyString())).thenReturn(true);
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHashedPassword");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    Usuario result = usuarioService.updateUsuario("test@test.com", usuarioUpdateDTO);

    assertNotNull(result);
    verify(senhaValidator, times(1)).isValid(anyString());
    verify(passwordEncoder, times(1)).encode(anyString());
    verify(usuarioRepository, times(1)).save(any(Usuario.class));
  }

  @Test
  void updateUsuario_WithInvalidNome_ShouldThrowValidationException() {
    usuarioUpdateDTO.setNome("123");
    usuarioUpdateDTO.setEmail(null);

    when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
    when(nomeValidator.isValid(anyString())).thenReturn(false);

    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> usuarioService.updateUsuario("test@test.com", usuarioUpdateDTO)
    );

    assertEquals("Nome inválido. Deve conter apenas letras e ter entre 2 e 100 caracteres", exception.getMessage());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void createUsuario_WithBiografiaContainingProhibitedWord_ShouldThrowValidationException() {
    usuarioCreateDTO.setBiografia("Texto com palavra idiota proibida");

    when(cpfValidator.isValid(anyString())).thenReturn(true);
    when(emailValidator.isValid(anyString())).thenReturn(true);
    when(nomeValidator.isValid(anyString())).thenReturn(true);
    when(palavrasProibidasService.contemPalavraProibida(usuarioCreateDTO.getNome())).thenReturn(false);
    when(palavrasProibidasService.contemPalavraProibida(usuarioCreateDTO.getBiografia())).thenReturn(true);
    when(palavrasProibidasService.identificarPalavraProibida(usuarioCreateDTO.getBiografia())).thenReturn("IDIOTA"); // Usar biografia aqui

    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> usuarioService.createUsuario(usuarioCreateDTO)
    );

    assertTrue(exception.getMessage().contains("Biografia contém conteúdo não permitido"));
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void obterRanking_WithValidEmail_ShouldReturnRankingResponse() {
    // Arrange
    String email = "test@test.com";

    // Mock do usuário logado
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

    // Mock das projections do top 50
    UsuarioRankingProjection projection1 = mock(UsuarioRankingProjection.class);
    when(projection1.getPosicao()).thenReturn(1L);
    when(projection1.getNome()).thenReturn("Usuario Top 1");
    when(projection1.getQntdXp()).thenReturn(5000);
    when(projection1.getNivel()).thenReturn(10);

    UsuarioRankingProjection projection2 = mock(UsuarioRankingProjection.class);
    when(projection2.getPosicao()).thenReturn(2L);
    when(projection2.getNome()).thenReturn("Usuario Top 2");
    when(projection2.getQntdXp()).thenReturn(4500);
    when(projection2.getNivel()).thenReturn(9);

    List<UsuarioRankingProjection> top50Projections = Arrays.asList(projection1, projection2);
    when(usuarioRepository.findTop50UsuariosPorXp()).thenReturn(top50Projections);

    when(usuarioRepository.findPosicaoNoRanking(BigInteger.valueOf(1))).thenReturn(15L);

    when(usuarioRepository.findXpRecebidoUltimos30Dias(BigInteger.valueOf(1))).thenReturn(250);

    RankingResponseDTO result = usuarioService.obterRanking(email);

    assertNotNull(result);
    assertNotNull(result.getRankingList());
    assertEquals(2, result.getRankingList().size());

    assertEquals(1L, result.getRankingList().get(0).getPosicao());
    assertEquals("Usuario Top 1", result.getRankingList().get(0).getNome());
    assertEquals(5000, result.getRankingList().get(0).getQntdXp());
    assertEquals(10, result.getRankingList().get(0).getNivel());

    assertEquals(2L, result.getRankingList().get(1).getPosicao());
    assertEquals("Usuario Top 2", result.getRankingList().get(1).getNome());
    assertEquals(4500, result.getRankingList().get(1).getQntdXp());
    assertEquals(9, result.getRankingList().get(1).getNivel());

    assertNotNull(result.getUsuarioLogado());
    assertEquals(15L, result.getUsuarioLogado().getPosicao());
    assertEquals(250, result.getUsuarioLogado().getXpRecebidoUltimos30Dias());

    verify(usuarioRepository, times(1)).findByEmail(email);
    verify(usuarioRepository, times(1)).findTop50UsuariosPorXp();
    verify(usuarioRepository, times(1)).findPosicaoNoRanking(BigInteger.valueOf(1));
    verify(usuarioRepository, times(1)).findXpRecebidoUltimos30Dias(BigInteger.valueOf(1));
  }

  @Test
  void obterRanking_WithInvalidEmail_ShouldThrowEntityNotFoundException() {
    String email = "invalido@test.com";
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> usuarioService.obterRanking(email)
    );

    assertEquals("Usuário não encontrado", exception.getMessage());
    verify(usuarioRepository, times(1)).findByEmail(email);
    verify(usuarioRepository, never()).findTop50UsuariosPorXp();
    verify(usuarioRepository, never()).findPosicaoNoRanking(any());
    verify(usuarioRepository, never()).findXpRecebidoUltimos30Dias(any());
  }

  @Test
  void obterRanking_WithUserInTop50_ShouldReturnCorrectData() {
    // Arrange
    String email = "test@test.com";

    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

    // Mock - usuário está em 10º lugar
    UsuarioRankingProjection projection = mock(UsuarioRankingProjection.class);
    when(projection.getPosicao()).thenReturn(10L);
    when(projection.getNome()).thenReturn("Test User");
    when(projection.getQntdXp()).thenReturn(3000);
    when(projection.getNivel()).thenReturn(8);

    when(usuarioRepository.findTop50UsuariosPorXp()).thenReturn(Arrays.asList(projection));
    when(usuarioRepository.findPosicaoNoRanking(BigInteger.valueOf(1))).thenReturn(10L);
    when(usuarioRepository.findXpRecebidoUltimos30Dias(BigInteger.valueOf(1))).thenReturn(500);

    // Act
    RankingResponseDTO result = usuarioService.obterRanking(email);

    // Assert
    assertNotNull(result);
    assertEquals(10L, result.getUsuarioLogado().getPosicao());
    assertEquals(500, result.getUsuarioLogado().getXpRecebidoUltimos30Dias());
    verify(usuarioRepository, times(1)).findByEmail(email);
  }

  @Test
  void obterRanking_WithUserOutsideTop50_ShouldStillReturnUserPosition() {
    // Arrange
    String email = "test@test.com";

    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

    // Top 50 não contém o usuário logado
    UsuarioRankingProjection projection = mock(UsuarioRankingProjection.class);
    when(projection.getPosicao()).thenReturn(1L);
    when(projection.getNome()).thenReturn("Usuario Top 1");
    when(projection.getQntdXp()).thenReturn(10000);
    when(projection.getNivel()).thenReturn(20);

    when(usuarioRepository.findTop50UsuariosPorXp()).thenReturn(Arrays.asList(projection));

    // Usuário está em 127º lugar (fora do top 50)
    when(usuarioRepository.findPosicaoNoRanking(BigInteger.valueOf(1))).thenReturn(127L);
    when(usuarioRepository.findXpRecebidoUltimos30Dias(BigInteger.valueOf(1))).thenReturn(150);

    // Act
    RankingResponseDTO result = usuarioService.obterRanking(email);

    // Assert
    assertNotNull(result);
    assertEquals(127L, result.getUsuarioLogado().getPosicao());
    assertEquals(150, result.getUsuarioLogado().getXpRecebidoUltimos30Dias());
    assertEquals(1, result.getRankingList().size());
    verify(usuarioRepository, times(1)).findPosicaoNoRanking(BigInteger.valueOf(1));
  }

  @Test
  void obterRanking_WithNoXpInLast30Days_ShouldReturnZero() {
    // Arrange
    String email = "test@test.com";

    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

    UsuarioRankingProjection projection = mock(UsuarioRankingProjection.class);
    when(projection.getPosicao()).thenReturn(1L);
    when(projection.getNome()).thenReturn("Usuario Top 1");
    when(projection.getQntdXp()).thenReturn(5000);
    when(projection.getNivel()).thenReturn(10);

    when(usuarioRepository.findTop50UsuariosPorXp()).thenReturn(Arrays.asList(projection));
    when(usuarioRepository.findPosicaoNoRanking(BigInteger.valueOf(1))).thenReturn(50L);

    when(usuarioRepository.findXpRecebidoUltimos30Dias(BigInteger.valueOf(1))).thenReturn(0);

    RankingResponseDTO result = usuarioService.obterRanking(email);

    assertNotNull(result);
    assertEquals(0, result.getUsuarioLogado().getXpRecebidoUltimos30Dias());
    verify(usuarioRepository, times(1)).findXpRecebidoUltimos30Dias(BigInteger.valueOf(1));
  }

  @Test
  void obterRanking_WithEmptyRanking_ShouldReturnEmptyTop50() {
    // Arrange
    String email = "test@test.com";

    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

    when(usuarioRepository.findTop50UsuariosPorXp()).thenReturn(Arrays.asList());
    when(usuarioRepository.findPosicaoNoRanking(BigInteger.valueOf(1))).thenReturn(1L);
    when(usuarioRepository.findXpRecebidoUltimos30Dias(BigInteger.valueOf(1))).thenReturn(0);

    RankingResponseDTO result = usuarioService.obterRanking(email);

    assertNotNull(result);
    assertTrue(result.getRankingList().isEmpty());
    assertEquals(1L, result.getUsuarioLogado().getPosicao());
    verify(usuarioRepository, times(1)).findTop50UsuariosPorXp();
  }

  @Test
  void obterRanking_ShouldConvertProjectionsToDTO() {
    // Arrange
    String email = "test@test.com";

    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

    // Criar 3 projections para verificar a conversão
    UsuarioRankingProjection p1 = mock(UsuarioRankingProjection.class);
    when(p1.getPosicao()).thenReturn(1L);
    when(p1.getNome()).thenReturn("User 1");
    when(p1.getQntdXp()).thenReturn(1000);
    when(p1.getNivel()).thenReturn(5);

    UsuarioRankingProjection p2 = mock(UsuarioRankingProjection.class);
    when(p2.getPosicao()).thenReturn(2L);
    when(p2.getNome()).thenReturn("User 2");
    when(p2.getQntdXp()).thenReturn(900);
    when(p2.getNivel()).thenReturn(4);

    UsuarioRankingProjection p3 = mock(UsuarioRankingProjection.class);
    when(p3.getPosicao()).thenReturn(3L);
    when(p3.getNome()).thenReturn("User 3");
    when(p3.getQntdXp()).thenReturn(800);
    when(p3.getNivel()).thenReturn(4);

    when(usuarioRepository.findTop50UsuariosPorXp())
      .thenReturn(Arrays.asList(p1, p2, p3));

    when(usuarioRepository.findPosicaoNoRanking(BigInteger.valueOf(1))).thenReturn(5L);
    when(usuarioRepository.findXpRecebidoUltimos30Dias(BigInteger.valueOf(1))).thenReturn(100);

    // Act
    RankingResponseDTO result = usuarioService.obterRanking(email);

    assertNotNull(result);
    assertEquals(3, result.getRankingList().size());

    // Verificar conversão correta de cada projection
    UsuarioRankingDTO dto1 = result.getRankingList().get(0);
    assertEquals(1L, dto1.getPosicao());
    assertEquals("User 1", dto1.getNome());
    assertEquals(1000, dto1.getQntdXp());
    assertEquals(5, dto1.getNivel());

    UsuarioRankingDTO dto2 = result.getRankingList().get(1);
    assertEquals(2L, dto2.getPosicao());
    assertEquals("User 2", dto2.getNome());

    UsuarioRankingDTO dto3 = result.getRankingList().get(2);
    assertEquals(3L, dto3.getPosicao());
    assertEquals("User 3", dto3.getNome());
  }

  @Test
  void obterDetalhesUsuario_ProprioUsuario_ShouldReturnTokens() {
    // Arrange
    BigInteger userId = BigInteger.valueOf(1);
    String email = "test@test.com";

    Usuario usuario = new Usuario();
    usuario.setId(userId);
    usuario.setEmail(email);
    usuario.setNome("Test User");
    usuario.setBiografia("Biografia de teste");
    usuario.setNivel(5);
    usuario.setQntdXp(1500L);
    usuario.setQntdToken(300L);
    usuario.setTags(Arrays.asList());

    var detalhesProjection = mock(br.com.escoladeti.api_know_hall.projection.usuario.UsuarioDetalhesProjection.class);
    when(detalhesProjection.getNome()).thenReturn("Test User");
    when(detalhesProjection.getBiografia()).thenReturn("Biografia de teste");
    when(detalhesProjection.getNivel()).thenReturn(5);
    when(detalhesProjection.getXp()).thenReturn(1500L);
    when(detalhesProjection.getTokens()).thenReturn(300L);
    when(detalhesProjection.getQtdPosts()).thenReturn(10);
    when(detalhesProjection.getQtdComentarios()).thenReturn(25);
    when(detalhesProjection.getQtdUpVotes()).thenReturn(50);
    when(detalhesProjection.getQtdSuperVotes()).thenReturn(5);
    when(detalhesProjection.getQtdWorkshops()).thenReturn(2);
    when(detalhesProjection.getImagemUrl()).thenReturn("https://example.com/imagem.jpg");

    when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
    when(usuarioRepository.findDetalhesUsuarioById(userId)).thenReturn(Optional.of(detalhesProjection));

    // Act
    var result = usuarioService.obterDetalhesUsuario(email, userId);

    // Assert
    assertNotNull(result);
    assertEquals("Test User", result.getNome());
    assertEquals("Biografia de teste", result.getBiografia());
    assertEquals(5, result.getNivel());
    assertEquals(1500L, result.getXp());
    assertEquals(300L, result.getTokens()); // Tokens devem ser retornados para o próprio usuário
    assertEquals(10, result.getQtdPosts());
    assertEquals(25, result.getQtdComentarios());
    assertEquals(50, result.getQtdUpVotes());
    assertEquals(5, result.getQtdSuperVotes());
    assertEquals(2, result.getQtdWorkshops());
    assertEquals("https://example.com/imagem.jpg", result.getImagemUrl());

    verify(usuarioRepository, times(1)).findById(userId);
    verify(usuarioRepository, times(1)).findDetalhesUsuarioById(userId);
  }

  @Test
  void obterDetalhesUsuario_OutroUsuario_ShouldNotReturnTokens() {
    // Arrange
    BigInteger userId = BigInteger.valueOf(2);
    String emailLogado = "logado@test.com";

    Usuario usuario = new Usuario();
    usuario.setId(userId);
    usuario.setEmail("outro@test.com"); // Email diferente do logado
    usuario.setNome("Outro User");
    usuario.setBiografia("Biografia do outro");
    usuario.setNivel(3);
    usuario.setQntdXp(800L);
    usuario.setQntdToken(150L);
    usuario.setTags(Arrays.asList());

    var detalhesProjection = mock(br.com.escoladeti.api_know_hall.projection.usuario.UsuarioDetalhesProjection.class);
    when(detalhesProjection.getNome()).thenReturn("Outro User");
    when(detalhesProjection.getBiografia()).thenReturn("Biografia do outro");
    when(detalhesProjection.getNivel()).thenReturn(3);
    when(detalhesProjection.getXp()).thenReturn(800L);
    when(detalhesProjection.getQtdPosts()).thenReturn(5);
    when(detalhesProjection.getQtdComentarios()).thenReturn(12);
    when(detalhesProjection.getQtdUpVotes()).thenReturn(20);
    when(detalhesProjection.getQtdSuperVotes()).thenReturn(2);
    when(detalhesProjection.getQtdWorkshops()).thenReturn(1);
    when(detalhesProjection.getImagemUrl()).thenReturn("https://example.com/outro.jpg");

    when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
    when(usuarioRepository.findDetalhesUsuarioById(userId)).thenReturn(Optional.of(detalhesProjection));

    // Act
    var result = usuarioService.obterDetalhesUsuario(emailLogado, userId);

    // Assert
    assertNotNull(result);
    assertEquals("Outro User", result.getNome());
    assertEquals("Biografia do outro", result.getBiografia());
    assertEquals(3, result.getNivel());
    assertEquals(800L, result.getXp());
    assertNull(result.getTokens()); // Tokens NÃO devem ser retornados para outro usuário
    assertEquals(5, result.getQtdPosts());
    assertEquals(12, result.getQtdComentarios());
    assertEquals(20, result.getQtdUpVotes());
    assertEquals(2, result.getQtdSuperVotes());
    assertEquals(1, result.getQtdWorkshops());
    assertEquals("https://example.com/outro.jpg", result.getImagemUrl());

    verify(usuarioRepository, times(1)).findById(userId);
    verify(usuarioRepository, times(1)).findDetalhesUsuarioById(userId);
  }

  @Test
  void obterDetalhesUsuario_UsuarioNaoEncontrado_ShouldThrowException() {
    // Arrange
    BigInteger userId = BigInteger.valueOf(999);
    String email = "test@test.com";

    when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> usuarioService.obterDetalhesUsuario(email, userId)
    );

    assertEquals("Usuário não encontrado", exception.getMessage());
    verify(usuarioRepository, times(1)).findById(userId);
    verify(usuarioRepository, never()).findDetalhesUsuarioById(any());
  }

  @Test
  void obterDetalhesUsuario_IdNulo_ShouldThrowIllegalArgumentException() {
    // Arrange
    String email = "test@test.com";

    // Act & Assert
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> usuarioService.obterDetalhesUsuario(email, null)
    );

    assertEquals("O ID do usuário não pode ser nulo.", exception.getMessage());
    verify(usuarioRepository, never()).findById(any());
    verify(usuarioRepository, never()).findDetalhesUsuarioById(any());
  }

  @Test
  void obterDetalhesUsuario_SemImagemPerfil_ShouldReturnNullImagemUrl() {
    // Arrange
    BigInteger userId = BigInteger.valueOf(1);
    String email = "test@test.com";

    Usuario usuario = new Usuario();
    usuario.setId(userId);
    usuario.setEmail(email);
    usuario.setNome("Test User");
    usuario.setTags(Arrays.asList());

    var detalhesProjection = mock(br.com.escoladeti.api_know_hall.projection.usuario.UsuarioDetalhesProjection.class);
    when(detalhesProjection.getNome()).thenReturn("Test User");
    when(detalhesProjection.getBiografia()).thenReturn(null);
    when(detalhesProjection.getNivel()).thenReturn(1);
    when(detalhesProjection.getXp()).thenReturn(0L);
    when(detalhesProjection.getTokens()).thenReturn(0L);
    when(detalhesProjection.getQtdPosts()).thenReturn(0);
    when(detalhesProjection.getQtdComentarios()).thenReturn(0);
    when(detalhesProjection.getQtdUpVotes()).thenReturn(0);
    when(detalhesProjection.getQtdSuperVotes()).thenReturn(0);
    when(detalhesProjection.getQtdWorkshops()).thenReturn(0);
    when(detalhesProjection.getImagemUrl()).thenReturn(null); // Sem imagem

    when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
    when(usuarioRepository.findDetalhesUsuarioById(userId)).thenReturn(Optional.of(detalhesProjection));

    // Act
    var result = usuarioService.obterDetalhesUsuario(email, userId);

    // Assert
    assertNotNull(result);
    assertNull(result.getImagemUrl()); // Imagem deve ser null
    assertEquals(0, result.getQtdPosts());
    assertEquals(0, result.getQtdComentarios());
    assertEquals(0, result.getQtdUpVotes());
    assertEquals(0, result.getQtdSuperVotes());
    assertEquals(0, result.getQtdWorkshops());

    verify(usuarioRepository, times(1)).findById(userId);
    verify(usuarioRepository, times(1)).findDetalhesUsuarioById(userId);
  }
}
