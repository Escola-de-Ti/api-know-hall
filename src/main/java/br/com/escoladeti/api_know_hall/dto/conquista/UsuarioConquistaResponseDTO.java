package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioConquistaResponseDTO {

  private BigInteger id;
  private ConquistaSimpleDTO conquista;
  private ConquistaTierResponseDTO tier;
  private LocalDateTime dataObtencao;
  private Integer progressoAtual;

  public static UsuarioConquistaResponseDTO fromEntity(UsuarioConquista uc) {
    return UsuarioConquistaResponseDTO.builder()
      .id(uc.getId())
      .conquista(ConquistaSimpleDTO.fromEntity(uc.getConquista()))
      .tier(ConquistaTierResponseDTO.fromEntity(uc.getConquistaTier()))
      .dataObtencao(uc.getDataObtencao())
      .progressoAtual(uc.getProgressoAtual())
      .build();
  }
}
