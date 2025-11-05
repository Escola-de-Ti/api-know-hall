package br.com.escoladeti.api_know_hall.dto.workshop;

import br.com.escoladeti.api_know_hall.entity.workshop.DescricaoWorkshop;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DescricaoWorkshopResponseDTO {

  private BigInteger id;
  private String tema;
  private String descricao;

  public static DescricaoWorkshopResponseDTO fromEntity(DescricaoWorkshop descricao) {
    if (descricao == null) {
      return null;
    }

    DescricaoWorkshopResponseDTO dto = new DescricaoWorkshopResponseDTO();
    dto.setId(descricao.getId());
    dto.setTema(descricao.getTema());
    dto.setDescricao(descricao.getDescricao());
    return dto;
  }
}
