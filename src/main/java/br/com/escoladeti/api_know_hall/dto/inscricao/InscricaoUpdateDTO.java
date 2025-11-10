package br.com.escoladeti.api_know_hall.dto.inscricao;

import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoUpdateDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusInscricao status;
}
