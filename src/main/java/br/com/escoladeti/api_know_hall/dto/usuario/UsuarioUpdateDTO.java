package br.com.escoladeti.api_know_hall.dto.usuario;

import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

  @Email(message = "Email deve ter um formato válido")
  private String email;

  @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres")
  private String cpf;

  @Size(max = 15, message = "Telefone deve ter no máximo 15 caracteres")
  private String telefone;

  @Size(max = 15, message = "Telefone 2 deve ter no máximo 15 caracteres")
  private String telefone2;

  @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
  private String nome;

  @Size(max = 500, message = "Biografia deve ter no máximo 500 caracteres")
  private String biografia;

  @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
  private String senha;

  private StatusUsuario statusUsuario;

  private TipoUsuario tipoUsuario;

  private List<BigInteger> tags;
}
