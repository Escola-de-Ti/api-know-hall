package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.enums.TierConquista;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConquistaProgressoResponseDTO {

  private ConquistaSimpleDTO conquista;
  private TierConquista maiorTierConquistado;
  private ConquistaTierResponseDTO proximoTier;
  private List<UsuarioConquistaResponseDTO> tiersConquistados;
  private boolean completa;
  private double percentualProgresso;

  public static ConquistaProgressoResponseDTO fromDTO(ConquistaProgressoDTO dto) {
    return ConquistaProgressoResponseDTO.builder()
      .conquista(ConquistaSimpleDTO.fromEntity(dto.getConquista()))
      .maiorTierConquistado(dto.getMaiorTierConquistado())
      .proximoTier(dto.getProximoTier() != null ?
        ConquistaTierResponseDTO.fromEntity(dto.getProximoTier()) : null)
      .tiersConquistados(dto.getTiersConquistados().stream()
        .map(UsuarioConquistaResponseDTO::fromEntity)
        .collect(Collectors.toList()))
      .completa(dto.isCompleta())
      .percentualProgresso(dto.getPercentualProgresso())
      .build();
  }
}
