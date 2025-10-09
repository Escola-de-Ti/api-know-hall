package br.com.escoladeti.api_know_hall.entity;

import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "USUARIO")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "cpf", nullable = false, unique = true, length = 11)
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

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
    name = "USUARIO_TAGS",
    joinColumns = @JoinColumn(name = "usuario_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
  )
  private List<Tag> tags = new ArrayList<>();

  public Usuario(UsuarioUpdateDTO dto) {
    this.email = dto.getEmail();
    this.cpf = dto.getCpf();
    this.telefone = dto.getTelefone();
    this.telefone2 = dto.getTelefone2();
    this.nome = dto.getNome();
    this.biografia = dto.getBiografia();
    this.senhaHash = dto.getSenha();
    this.idImagemPerfil = dto.getIdImagemPerfil();
    this.statusUsuario = dto.getStatusUsuario();
    this.tipoUsuario = dto.getTipoUsuario();
    this.tags = dto.getTags();
  }

  public Usuario(UsuarioCreateDTO dto) {
    this.id = null;
    this.email = dto.getEmail();
    this.cpf = dto.getCpf();
    this.telefone = dto.getTelefone();
    this.telefone2 = dto.getTelefone2();
    this.nome = dto.getNome();
    this.biografia = dto.getBiografia();
    this.senhaHash = dto.getSenha();
    this.idImagemPerfil = dto.getIdImagemPerfil();
    this.statusUsuario = StatusUsuario.CONFIRMACAO_PENDENTE;
    this.tipoUsuario = dto.getTipoUsuario();
    this.tags = dto.getTags();
  }
}
