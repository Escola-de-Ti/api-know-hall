package br.com.escoladeti.api_know_hall.dto.inscricao;

import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InscricaoResponseDTO {

  private BigInteger id;
  private BigInteger usuarioId;
  private String usuarioNome;
  private BigInteger workshopId;
  private String workshopTitulo;
  private StatusInscricao status;
  private LocalDateTime dataInscricao;
}
