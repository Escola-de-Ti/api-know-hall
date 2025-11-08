package br.com.escoladeti.api_know_hall.imagem;

import br.com.escoladeti.api_know_hall.controller.ImagemController;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.exception.handler.GlobalExceptionHandler;
import br.com.escoladeti.api_know_hall.service.ImagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ImagemControllerTest {

  private ImagemService imagemService;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    imagemService = Mockito.mock(ImagemService.class);
    ImagemController controller = new ImagemController(imagemService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
  }

  @Test
  void uploadImage_success_returnsImagem() throws Exception {
    byte[] imageBytes = new byte[]{1, 2, 3};
    String type = "perfil";
    String principalName = "user123";
    Imagem imagem = new Imagem(BigInteger.ONE, principalName, "url", "idImagem", "path");
    when(imagemService.uploadImage(eq(imageBytes), eq(principalName), eq(type), any())).thenReturn(imagem);

    mockMvc.perform(post("/api/imagem/upload")
        .content(imageBytes)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .param("type", type)
        .principal(() -> principalName))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.nome").value(principalName))
      .andExpect(jsonPath("$.url").value("url"))
      .andExpect(jsonPath("$.idImagem").value("idImagem"))
      .andExpect(jsonPath("$.path").value("path"));
  }

  @Test
  void uploadImage_serviceThrows_returns500() throws Exception {
    byte[] imageBytes = new byte[]{1, 2, 3};
    String type = "perfil";
    String principalName = "user123";
    when(imagemService.uploadImage(any(), any(), any(), any())).thenThrow(new RuntimeException("erro"));

    mockMvc.perform(post("/api/imagem/upload")
        .content(imageBytes)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .param("type", type)
        .principal(() -> principalName))
      .andExpect(status().isInternalServerError());
  }

  @Test
  void deleteImage_success_returnsNoContent() throws Exception {
    BigInteger id = BigInteger.ONE;
    doNothing().when(imagemService).deleteImage(id);

    mockMvc.perform(delete("/api/imagem/delete/{id}", id))
      .andExpect(status().isNoContent());
  }

  @Test
  void deleteImage_serviceThrows_returns500() throws Exception {
    BigInteger id = BigInteger.ONE;
    doThrow(new RuntimeException("erro")).when(imagemService).deleteImage(id);

    mockMvc.perform(delete("/api/imagem/delete/{id}", id))
      .andExpect(status().isInternalServerError());
  }
}
