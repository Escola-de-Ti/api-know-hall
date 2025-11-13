package br.com.escoladeti.api_know_hall.dto;

import br.com.escoladeti.api_know_hall.entity.ImagemPost;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class ImagemPostDTO {

  private BigInteger id;
  private BigInteger imagemId;
  private String urlImagem;
  private Integer ordemImagem;

  public ImagemPostDTO() {
  }

  public ImagemPostDTO(BigInteger id, BigInteger imagemId, String urlImagem, Integer ordemImagem) {
    this.id = id;
    this.imagemId = imagemId;
    this.urlImagem = urlImagem;
    this.ordemImagem = ordemImagem;
  }

  public static ImagemPostDTO fromEntity(ImagemPost imagemPost) {
    return new ImagemPostDTO(
      imagemPost.getId(),
      imagemPost.getImagem().getId(),
      imagemPost.getImagem().getUrl(),
      imagemPost.getOrdemImagem()
    );
  }
}
