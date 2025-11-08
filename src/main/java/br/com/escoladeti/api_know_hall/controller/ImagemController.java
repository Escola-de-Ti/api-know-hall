package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.service.ImagemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.Principal;

@RestController
@RequestMapping("/api/imagem")
public class ImagemController {

  private final ImagemService imagemService;

  public ImagemController(ImagemService imagemService) {
    this.imagemService = imagemService;
  }

  @PostMapping("/upload")
  public ResponseEntity<Imagem> upload(
    @RequestBody byte[] imagem, @RequestParam("type") String type,
    @RequestParam(value = "id_type", required = false) String idType,
    Principal principal
  ) {
    Imagem url = imagemService.uploadImage(imagem, principal.getName(), type, idType);
    return ResponseEntity.ok(url);
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> delete(@PathVariable BigInteger id) {

    imagemService.deleteImage(id);
    return ResponseEntity.noContent().build();
  }
}
