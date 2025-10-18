package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import lombok.*;

import java.math.BigInteger;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConquistaTierResponseDTO {

  private BigInteger id;
  private TierConquista tier;
  private Integer quantidadeNecessaria;
  private String descricaoTier;
  private String corHex;
  private Integer nivel;

  public static ConquistaTierResponseDTO fromEntity(ConquistaTier tier) {
    return ConquistaTierResponseDTO.builder()
      .id(tier.getId())
      .tier(tier.getTier())
      .quantidadeNecessaria(tier.getQuantidadeNecessaria())
      .descricaoTier(tier.getDescricaoTier())
      .nivel(tier.getTier().getNivel())
      .build();
  }
}
