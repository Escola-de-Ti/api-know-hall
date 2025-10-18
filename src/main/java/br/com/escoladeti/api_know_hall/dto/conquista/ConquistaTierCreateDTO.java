package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.enums.TierConquista;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConquistaTierCreateDTO {

  @NotNull(message = "Tier é obrigatório")
  private TierConquista tier;

  @NotNull(message = "Quantidade necessária é obrigatória")
  @Min(value = 1, message = "Quantidade necessária deve ser maior que zero")
  private Integer quantidadeNecessaria;

  private String descricaoTier;
}
