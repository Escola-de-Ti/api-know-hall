package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConquistaSimpleDTO {
  private BigInteger id;
  private String nome;
  private String descricao;
  private TipoConquista tipoConquista;
  private String iconeUrl;

  public static ConquistaSimpleDTO fromEntity(Conquista conquista) {
    return ConquistaSimpleDTO.builder()
      .id(conquista.getId())
      .nome(conquista.getNome())
      .descricao(conquista.getDescricao())
      .tipoConquista(conquista.getTipoConquista())
      .iconeUrl(conquista.getIconeUrl())
      .build();
  }
}
