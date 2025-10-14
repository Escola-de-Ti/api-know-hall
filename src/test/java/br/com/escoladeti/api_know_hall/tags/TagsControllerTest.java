package br.com.escoladeti.api_know_hall.tags;

import br.com.escoladeti.api_know_hall.controller.TagsController;
import br.com.escoladeti.api_know_hall.dto.tags.TagCreateDTO;
import br.com.escoladeti.api_know_hall.entity.Tag;

import br.com.escoladeti.api_know_hall.exception.PalavraProibidaException;
import br.com.escoladeti.api_know_hall.exception.handler.GlobalExceptionHandler;
import br.com.escoladeti.api_know_hall.service.TagsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TagsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)  // ✅ IMPORTANTE
class TagsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TagsService tagsService;

  @Test
  void deveCriarTagComSucesso() throws Exception {
    // Arrange
    TagCreateDTO createDTO = new TagCreateDTO("Java");
    Tag tag = new Tag();
    tag.setId(BigInteger.ONE);
    tag.setName("JAVA");

    when(tagsService.createOrGetExisting("Java")).thenReturn(tag);

    // Act & Assert
    mockMvc.perform(post("/api/tags")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
      .andDo(print())  // ✅ Para debug
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.name").value("JAVA"));
  }

  @Test
  void deveRetornarBadRequestQuandoNomeVazio() throws Exception {
    // Arrange
    TagCreateDTO createDTO = new TagCreateDTO("");

    // Act & Assert
    mockMvc.perform(post("/api/tags")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveRetornarBadRequestQuandoNomeNulo() throws Exception {
    // Arrange
    TagCreateDTO createDTO = new TagCreateDTO(null);

    // Act & Assert
    mockMvc.perform(post("/api/tags")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveRetornarBadRequestQuandoNomeMuitoCurto() throws Exception {
    // Arrange
    TagCreateDTO createDTO = new TagCreateDTO("A");

    // Act & Assert
    mockMvc.perform(post("/api/tags")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveRetornarBadRequestQuandoNomeMuitoLongo() throws Exception {
    // Arrange
    String nomeLongo = "A".repeat(51);
    TagCreateDTO createDTO = new TagCreateDTO(nomeLongo);

    // Act & Assert
    mockMvc.perform(post("/api/tags")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveRetornarBadRequestQuandoPalavraProibida() throws Exception {
    // Arrange
    TagCreateDTO createDTO = new TagCreateDTO("palavrao");

    when(tagsService.createOrGetExisting("palavrao"))
      .thenThrow(new PalavraProibidaException("Conteúdo inapropriado"));

    // Act & Assert
    mockMvc.perform(post("/api/tags")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDTO)))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  void deveRetornarTagsPopularesComSucesso() throws Exception {
    // Arrange
    Tag tag1 = new Tag();
    tag1.setId(BigInteger.ONE);
    tag1.setName("JAVA");

    Tag tag2 = new Tag();
    tag2.setId(BigInteger.TWO);
    tag2.setName("PYTHON");

    List<Tag> tags = Arrays.asList(tag1, tag2);

    when(tagsService.findMostPopular(10)).thenReturn(tags);

    // Act & Assert
    mockMvc.perform(get("/api/tags/popular"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].name").value("JAVA"))
      .andExpect(jsonPath("$[1].name").value("PYTHON"));
  }

  @Test
  void deveRetornarListaVaziaQuandoNaoHaTagsPopulares() throws Exception {
    // Arrange
    when(tagsService.findMostPopular(10)).thenReturn(Arrays.asList());

    // Act & Assert
    mockMvc.perform(get("/api/tags/popular"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(0));
  }
}
