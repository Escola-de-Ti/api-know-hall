package br.com.escoladeti.api_know_hall.dto.inscricao;

import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoResponseDTO {

    private BigInteger id;
    private BigInteger usuarioId;
    private String usuarioNome;
    private BigInteger workshopId;
    private String workshopTitulo;
    private StatusInscricao status;
    private LocalDateTime dataInscricao;
}
