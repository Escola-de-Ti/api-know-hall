package br.com.escoladeti.api_know_hall.dto.inscricao;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoCreateDTO {

    @NotNull(message = "O ID do workshop é obrigatório")
    private BigInteger workshopId;
}
