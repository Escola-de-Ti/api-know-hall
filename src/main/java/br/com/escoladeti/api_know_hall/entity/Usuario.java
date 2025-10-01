package br.com.escoladeti.api_know_hall.entity;

import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USUARIO")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id_usuario", nullable = false)
  private Integer idUsuario;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "cpf", nullable = false)
  private String cpf;

  @Column(name = "telefone")
  private String telefone;

  @Column(name = "telefone2")
  private String telefone2;

  @Column(name = "nome", nullable = false)
  private String nome;

  @Column(name = "sobrenome")
  private String biografia;

  @Column(name = "senha_hash", nullable = false)
  private String senhaHash;

  @Column(name = "id_imagem_perfil")
  private Integer idImagemPerfil;

  @Column(name = "status_usuario", nullable = false)
  @Enumerated(EnumType.STRING)
  private StatusUsuario statusUsuario;

  @Column(name = "tipo_usuario", nullable = false)
  @Enumerated(EnumType.STRING)
  private TipoUsuario tipoUsuario;


}
