package br.com.escoladeti.api_know_hall.imagem;

import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.repository.ImagemRepository;
import br.com.escoladeti.api_know_hall.service.ImagemService;
import br.com.escoladeti.api_know_hall.exception.handler.GlobalExceptionHandler;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ImagemServiceTest {

  private ImagemRepository imagemRepository;
  private UsuarioService usuarioService;
  private HttpClient httpClient;
  private ImagemService imagemService;

  private final String SUPABASE = "https://supabase.example.com";
  private final String TOKEN = "token123";

  @BeforeEach
  void setup() {
    imagemRepository = mock(ImagemRepository.class);
    usuarioService = mock(UsuarioService.class);
    httpClient = mock(HttpClient.class);

    imagemService = new ImagemService(SUPABASE, TOKEN);

    try {
      var fRepo = ImagemService.class.getDeclaredField("imagemRepository");
      fRepo.setAccessible(true);
      fRepo.set(imagemService, imagemRepository);

      var fUser = ImagemService.class.getDeclaredField("usuarioService");
      fUser.setAccessible(true);
      fUser.set(imagemService, usuarioService);

      // httpClient is private final; remove final modifier via reflection before setting
      Field fHttp = ImagemService.class.getDeclaredField("httpClient");
      fHttp.setAccessible(true);
      try {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(fHttp, fHttp.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
      } catch (NoSuchFieldException ignored) {
        // on newer JVMs the "modifiers" field may not be present; attempt to set anyway
      }
      fHttp.set(imagemService, httpClient);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void uploadImage_success_nonPerfil_callsSaveAndReturnsImagem() throws Exception {
    byte[] bytes = new byte[]{1, 2, 3};
    String userEmail = "user@example.com";
    String type = "outro";
    String idType = "";

    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn("{\"Key\": \"path/key\", \"Id\": \"id123\"}");

    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

    when(imagemRepository.save(any(Imagem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(imagemRepository.findByIdImagem(anyString())).thenReturn(Optional.empty());

    Imagem result = imagemService.uploadImage(bytes, userEmail, type, idType);

    // captura o Imagem salvo e validações que não dependem do UUID
    var captor = org.mockito.ArgumentCaptor.forClass(Imagem.class);
    verify(imagemRepository).save(captor.capture());
    Imagem saved = captor.getValue();

    assertNotNull(result);
    assertSame(saved, result);
    assertTrue(saved.getNome().startsWith(userEmail + idType));
    assertTrue(saved.getUrl().contains(SUPABASE + "/assets/" + type + "/"));
    assertEquals("path/key", saved.getPath());
    assertEquals("id123", saved.getIdImagem());

    verify(usuarioService, never()).atualizarImagemPerfil(anyString(), any(Imagem.class));
  }

  @Test
  void uploadImage_httpError_throwsIllegalStateException() throws Exception {
    byte[] bytes = new byte[]{1, 2, 3};
    String imageName = "image.png";
    String type = "outro";
    String idType = null;

    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(500);
    when(response.body()).thenReturn("error");

    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> imagemService.uploadImage(bytes, imageName, type, idType));
    assertTrue(
      ex.getMessage().contains("Falha ao enviar imagem") ||
        ex.getMessage().contains("Erro ao enviar imagem para Supabase")
    );
    verify(imagemRepository, never()).save(any(Imagem.class));
  }

  @Test
  void deleteImage_success_callsHttpClient() throws Exception {
    BigInteger id = BigInteger.ONE;
    Imagem img = new Imagem(id, "name", SUPABASE + "/assets/outro/name", "id123", "path/key");

    when(imagemRepository.findById(id)).thenReturn(Optional.of(img));

    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn("ok");

    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

    imagemService.deleteImage(id);

    verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  void deleteImage_notFound_throwsIllegalStateException() {
    BigInteger id = BigInteger.ONE;
    when(imagemRepository.findById(id)).thenReturn(Optional.empty());

    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> imagemService.deleteImage(id));
    assertTrue(ex.getMessage().contains("Imagem não encontrada"));
  }

}
