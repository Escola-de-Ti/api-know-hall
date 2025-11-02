package br.com.escoladeti.api_know_hall.conquista;

import br.com.escoladeti.api_know_hall.controller.ConquistaController;
import br.com.escoladeti.api_know_hall.dto.conquista.ConquistaCreateDTO;
import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.service.ConquistaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ConquistaController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class ConquistaControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ConquistaService conquistaService;

  @Autowired
  private ObjectMapper objectMapper;

  private Conquista conquista;
  private ConquistaCreateDTO conquistaCreateDTO;

  @BeforeEach
  void setUp() {
    conquista = new Conquista();
    conquista.setId(BigInteger.valueOf(1));
    conquista.setNome("Participante Ativo");
    conquista.setDescricao("Participe de eventos");
    conquista.setTipoConquista(TipoConquista.INSIGNIA);
    conquista.setCampoValidacao("participacoes");

    conquistaCreateDTO = new ConquistaCreateDTO();
    conquistaCreateDTO.setNome("Participante Ativo");
    conquistaCreateDTO.setDescricao("Participe de eventos");
    conquistaCreateDTO.setTipoConquista(TipoConquista.INSIGNIA);
    conquistaCreateDTO.setCampoValidacao("participacoes");

    Map<TierConquista, Integer> tiers = new HashMap<>();
    tiers.put(TierConquista.BRONZE, 10);
    conquistaCreateDTO.setTiers(tiers);
  }

  @Test
  void listarConquistas_ShouldReturnListOfConquistas() throws Exception {
    List<Conquista> conquistas = Arrays.asList(conquista);
    when(conquistaService.listarTodasConquistas()).thenReturn(conquistas);

    mockMvc.perform(get("/api/conquistas"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].nome").value("Participante Ativo"));

    verify(conquistaService, times(1)).listarTodasConquistas();
  }

  @Test
  void listarConquistas_WithTipoFilter_ShouldReturnFilteredConquistas() throws Exception {
    List<Conquista> conquistas = Arrays.asList(conquista);
    when(conquistaService.listarConquistasPorTipo(TipoConquista.INSIGNIA))
      .thenReturn(conquistas);

    mockMvc.perform(get("/api/conquistas")
        .param("tipo", "INSIGNIA"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].tipoConquista").value("INSIGNIA"));

    verify(conquistaService, times(1)).listarConquistasPorTipo(TipoConquista.INSIGNIA);
  }

  @Test
  void listarConquistas_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
    when(conquistaService.listarTodasConquistas())
      .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(get("/api/conquistas"))
      .andExpect(status().isInternalServerError());

    verify(conquistaService, times(1)).listarTodasConquistas();
  }

  @Test
  void buscarConquista_WithValidId_ShouldReturnConquista() throws Exception {
    when(conquistaService.buscarConquistaPorId(BigInteger.valueOf(1))).thenReturn(conquista);

    mockMvc.perform(get("/api/conquistas/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.nome").value("Participante Ativo"));

    verify(conquistaService, times(1)).buscarConquistaPorId(BigInteger.valueOf(1));
  }

  @Test
  void buscarConquista_WithInvalidId_ShouldReturnNotFound() throws Exception {
    when(conquistaService.buscarConquistaPorId(BigInteger.valueOf(999)))
      .thenThrow(new EntityNotFoundException("Conquista não encontrada"));

    mockMvc.perform(get("/api/conquistas/999"))
      .andExpect(status().isNotFound());

    verify(conquistaService, times(1)).buscarConquistaPorId(BigInteger.valueOf(999));
  }

  @Test
  void criarConquista_WithValidData_ShouldReturnCreatedConquista() throws Exception {
    when(conquistaService.criarConquistaComTiers(any(), any(), any(), any(), any()))
      .thenReturn(conquista);

    mockMvc.perform(post("/api/conquistas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(conquistaCreateDTO)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.nome").value("Participante Ativo"));

    verify(conquistaService, times(1)).criarConquistaComTiers(any(), any(), any(), any(), any());
  }

  @Test
  void criarConquista_WithInvalidData_ShouldReturnBadRequest() throws Exception {
    conquistaCreateDTO.setNome(""); // Nome vazio

    mockMvc.perform(post("/api/conquistas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(conquistaCreateDTO)))
      .andExpect(status().isBadRequest());

    verify(conquistaService, never()).criarConquistaComTiers(any(), any(), any(), any(), any());
  }

  @Test
  void listarPorCampo_ShouldReturnConquistasByCampo() throws Exception {
    List<Conquista> conquistas = Arrays.asList(conquista);
    when(conquistaService.listarConquistasPorCampo("participacoes"))
      .thenReturn(conquistas);

    mockMvc.perform(get("/api/conquistas/campo/participacoes"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].campoValidacao").value("participacoes"));

    verify(conquistaService, times(1)).listarConquistasPorCampo("participacoes");
  }
}
