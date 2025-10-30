package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.service.ImagemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storage")
public class ImagemController {

  private final ImagemService imagemService;

  public ImagemController(ImagemService imagemService) {
    this.imagemService = imagemService;
  }

  @PostMapping("/upload")
  public ResponseEntity<Imagem> upload(@RequestBody byte[] imagem, @RequestParam(value = "imageName", required = false) String imageName) {
    Imagem url = imagemService.uploadImage(imagem, imageName);
    return ResponseEntity.ok(url);
  }
}
