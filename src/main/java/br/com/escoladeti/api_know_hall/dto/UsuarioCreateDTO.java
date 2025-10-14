package br.com.escoladeti.api_know_hall.dto;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioCreateDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres")
    private String cpf;

    @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres")
    private String telefone;

    @Size(max = 13, message = "Telefone deve ter no máximo 14 caracteres")
    private String telefone2;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String biografia;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    private Integer idImagemPerfil;

    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipoUsuario;

    private List<Tag> tags = new ArrayList<>();
}
