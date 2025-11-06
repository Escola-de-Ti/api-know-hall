package br.com.escoladeti.api_know_hall.conquista;

import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.controller.UsuarioConquistaController;
import br.com.escoladeti.api_know_hall.dto.conquista.ConquistaProgressoDTO;
import br.com.escoladeti.api_know_hall.dto.conquista.VerificarProgressoDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.service.ConquistaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = UsuarioConquistaController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class
  },
  excludeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = JwtAuthenticationFilter.class
  )
)
@ActiveProfiles("test")
class UsuarioConquistaControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ConquistaService conquistaService;

  @Autowired
  private ObjectMapper objectMapper;

  private UsuarioConquista usuarioConquista;
  private Conquista conquista;
  private ConquistaTier tier;
  private Usuario usuario;
  private VerificarProgressoDTO verificarProgressoDTO;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(BigInteger.valueOf(1));
    usuario.setNome("Test User");

    conquista = new Conquista();
    conquista.setId(BigInteger.valueOf(1));
    conquista.setNome("Participante Ativo");
    conquista.setDescricao("Descrição da conquista");
    conquista.setTipoConquista(TipoConquista.INSIGNIA);
    conquista.setCampoValidacao("participacoes");
    conquista.setIconeUrl("https://example.com/icon.png");
    conquista.setTiers(new ArrayList<>());

    tier = new ConquistaTier();
    tier.setId(BigInteger.valueOf(1));
    tier.setTier(TierConquista.BRONZE);
    tier.setQuantidadeNecessaria(10);
    tier.setConquista(conquista);

    usuarioConquista = new UsuarioConquista();
    usuarioConquista.setId(BigInteger.valueOf(1));
    usuarioConquista.setUsuario(usuario);
    usuarioConquista.setConquista(conquista);
    usuarioConquista.setConquistaTier(tier);
    usuarioConquista.setProgressoAtual(10);
    usuarioConquista.setDataObtencao(LocalDateTime.now());

    verificarProgressoDTO = new VerificarProgressoDTO();
    verificarProgressoDTO.setCampoValidacao("participacoes");
    verificarProgressoDTO.setProgressoAtual(15);
  }

  @Test
  void listarConquistasUsuario_ShouldReturnUserConquistas() throws Exception {
    List<UsuarioConquista> conquistas = Arrays.asList(usuarioConquista);
    when(conquistaService.listarConquistasUsuario(BigInteger.valueOf(1)))
      .thenReturn(conquistas);

    mockMvc.perform(get("/api/usuarios/1/conquistas"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].conquista.nome").value("Participante Ativo"));

    verify(conquistaService, times(1)).listarConquistasUsuario(BigInteger.valueOf(1));
  }

  @Test
  void listarConquistasUsuario_WithTipoFilter_ShouldReturnFilteredConquistas() throws Exception {
    List<UsuarioConquista> conquistas = Arrays.asList(usuarioConquista);
    when(conquistaService.listarConquistasUsuarioPorTipo(BigInteger.valueOf(1), TipoConquista.INSIGNIA))
      .thenReturn(conquistas);

    mockMvc.perform(get("/api/usuarios/1/conquistas")
        .param("tipo", "INSIGNIA"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].conquista.tipoConquista").value("INSIGNIA"));

    verify(conquistaService, times(1))
      .listarConquistasUsuarioPorTipo(BigInteger.valueOf(1), TipoConquista.INSIGNIA);
  }

  @Test
  void obterProgresso_ShouldReturnProgressDetails() throws Exception {
    ConquistaProgressoDTO progresso = new ConquistaProgressoDTO(
      conquista,
      TierConquista.BRONZE,
      tier,
      Arrays.asList(usuarioConquista)
    );

    when(conquistaService.obterProgressoConquista(BigInteger.valueOf(1), BigInteger.valueOf(1)))
      .thenReturn(progresso);

    mockMvc.perform(get("/api/usuarios/1/conquistas/1/progresso"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.conquista.nome").value("Participante Ativo"))
      .andExpect(jsonPath("$.maiorTierConquistado").value("BRONZE"));

    verify(conquistaService, times(1))
      .obterProgressoConquista(BigInteger.valueOf(1), BigInteger.valueOf(1));
  }

  @Test
  void verificarEConcederConquistas_ShouldProcessSuccessfully() throws Exception {
    doNothing().when(conquistaService)
      .verificarEConcederConquistas(any(), any(), any());

    mockMvc.perform(post("/api/usuarios/1/conquistas/verificar")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(verificarProgressoDTO)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.mensagem").value("Progresso verificado com sucesso"))
      .andExpect(jsonPath("$.usuarioId").value(1))
      .andExpect(jsonPath("$.campoValidacao").value("participacoes"))
      .andExpect(jsonPath("$.progressoAtual").value(15));

    verify(conquistaService, times(1))
      .verificarEConcederConquistas(BigInteger.valueOf(1), "participacoes", 15);
  }

  @Test
  void verificarEConcederConquistas_WithInvalidData_ShouldReturnBadRequest() throws Exception {
    verificarProgressoDTO.setCampoValidacao(""); // Campo vazio

    mockMvc.perform(post("/api/usuarios/1/conquistas/verificar")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(verificarProgressoDTO)))
      .andExpect(status().isBadRequest());

    verify(conquistaService, never()).verificarEConcederConquistas(any(), any(), any());
  }

  @Test
  void verificarEConcederConquistas_WithNegativeProgress_ShouldReturnBadRequest() throws Exception {
    verificarProgressoDTO.setProgressoAtual(-5);

    mockMvc.perform(post("/api/usuarios/1/conquistas/verificar")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(verificarProgressoDTO)))
      .andExpect(status().isBadRequest());

    verify(conquistaService, never()).verificarEConcederConquistas(any(), any(), any());
  }

  @Test
  void obterEstatisticas_ShouldReturnUserStatistics() throws Exception {
    // Configurar conquista de insígnia completa
    Conquista conquistaInsignia = new Conquista();
    conquistaInsignia.setId(BigInteger.valueOf(1));
    conquistaInsignia.setNome("Participante Ativo");
    conquistaInsignia.setDescricao("Descrição");
    conquistaInsignia.setTipoConquista(TipoConquista.INSIGNIA);
    conquistaInsignia.setCampoValidacao("participacoes");
    conquistaInsignia.setTiers(new ArrayList<>());

    ConquistaTier tierBronze = new ConquistaTier();
    tierBronze.setId(BigInteger.valueOf(1));
    tierBronze.setTier(TierConquista.BRONZE);
    tierBronze.setQuantidadeNecessaria(10);
    tierBronze.setConquista(conquistaInsignia);

    UsuarioConquista insignia = new UsuarioConquista();
    insignia.setId(BigInteger.valueOf(1));
    insignia.setUsuario(usuario);
    insignia.setConquista(conquistaInsignia);
    insignia.setConquistaTier(tierBronze);
    insignia.setProgressoAtual(10);
    insignia.setDataObtencao(LocalDateTime.now());

    // Configurar conquista de certificado completa
    Conquista conquistaCertificado = new Conquista();
    conquistaCertificado.setId(BigInteger.valueOf(2));
    conquistaCertificado.setNome("Certificado Java");
    conquistaCertificado.setDescricao("Concluiu workshop");
    conquistaCertificado.setTipoConquista(TipoConquista.CERTIFICADO);
    conquistaCertificado.setCampoValidacao("conclusao_workshop");
    conquistaCertificado.setTiers(new ArrayList<>());

    ConquistaTier tierOuro = new ConquistaTier();
    tierOuro.setId(BigInteger.valueOf(2));
    tierOuro.setTier(TierConquista.OURO);
    tierOuro.setQuantidadeNecessaria(1);
    tierOuro.setConquista(conquistaCertificado);

    UsuarioConquista ucCert = new UsuarioConquista();
    ucCert.setId(BigInteger.valueOf(2));
    ucCert.setUsuario(usuario);
    ucCert.setConquista(conquistaCertificado);
    ucCert.setConquistaTier(tierOuro);
    ucCert.setProgressoAtual(1);
    ucCert.setDataObtencao(LocalDateTime.now());

    List<UsuarioConquista> conquistas = Arrays.asList(insignia, ucCert);
    when(conquistaService.listarConquistasUsuario(BigInteger.valueOf(1)))
      .thenReturn(conquistas);

    mockMvc.perform(get("/api/usuarios/1/conquistas/estatisticas"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalConquistas").value(2))
      .andExpect(jsonPath("$.totalInsignias").value(1))
      .andExpect(jsonPath("$.totalCertificados").value(1))
      .andExpect(jsonPath("$.conquistasPorTier.BRONZE").value(1))
      .andExpect(jsonPath("$.conquistasPorTier.OURO").value(1));

    verify(conquistaService, times(1)).listarConquistasUsuario(BigInteger.valueOf(1));
  }

  @Test
  void obterEstatisticas_WithNoConquistas_ShouldReturnEmptyStatistics() throws Exception {
    when(conquistaService.listarConquistasUsuario(BigInteger.valueOf(1)))
      .thenReturn(new ArrayList<>());

    mockMvc.perform(get("/api/usuarios/1/conquistas/estatisticas"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalConquistas").value(0))
      .andExpect(jsonPath("$.totalInsignias").value(0))
      .andExpect(jsonPath("$.totalCertificados").value(0));

    verify(conquistaService, times(1)).listarConquistasUsuario(BigInteger.valueOf(1));
  }

  @Test
  void listarConquistasUsuario_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    when(conquistaService.listarConquistasUsuario(BigInteger.valueOf(1)))
      .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(get("/api/usuarios/1/conquistas"))
      .andExpect(status().isInternalServerError());

    verify(conquistaService, times(1)).listarConquistasUsuario(BigInteger.valueOf(1));
  }
}
