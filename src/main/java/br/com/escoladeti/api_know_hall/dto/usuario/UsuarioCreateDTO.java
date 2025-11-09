package br.com.escoladeti.api_know_hall.dto.usuario;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {

  @NotBlank(message = "Email é obrigatório")
  @Email(message = "Email deve ter um formato válido")
  private String email;

  @NotBlank(message = "CPF é obrigatório")
  @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres")
  private String cpf;

  @Size(max = 15, message = "Telefone deve ter no máximo 15 caracteres")
  private String telefone;

  @Size(max = 15, message = "Telefone 2 deve ter no máximo 15 caracteres")
  private String telefone2;

  @NotBlank(message = "Nome é obrigatório")
  @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
  private String nome;

  @Size(max = 500, message = "Biografia deve ter no máximo 500 caracteres")
  private String biografia;

  @NotBlank(message = "Senha é obrigatória")
  @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
  private String senha;

  private String imageBase64;

  @NotNull(message = "Tipo de usuário é obrigatório")
  private TipoUsuario tipoUsuario;

  private List<Tag> tags = new ArrayList<>();
}
