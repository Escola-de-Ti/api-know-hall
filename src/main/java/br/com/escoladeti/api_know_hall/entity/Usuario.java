package br.com.escoladeti.api_know_hall.entity;

import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import java.math.BigInteger;

@Entity
@Table(name = "USUARIO")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

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

  public Usuario() {
  }

  public Usuario(BigInteger id, String email, String cpf, String telefone, String telefone2, String nome, String biografia, String senhaHash, Integer idImagemPerfil, StatusUsuario statusUsuario, TipoUsuario tipoUsuario, List<Tag> tags) {
    this.id = id;
    this.email = email;
    this.cpf = cpf;
    this.telefone = telefone;
    this.telefone2 = telefone2;
    this.nome = nome;
    this.biografia = biografia;
    this.senhaHash = senhaHash;
    this.idImagemPerfil = idImagemPerfil;
    this.statusUsuario = statusUsuario;
    this.tipoUsuario = tipoUsuario;
    this.tags = tags;
  }

  public BigInteger getId() {
    return id;
  }

  public void setId(BigInteger id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
  }

  public String getTelefone2() {
    return telefone2;
  }

  public void setTelefone2(String telefone2) {
    this.telefone2 = telefone2;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getBiografia() {
    return biografia;
  }

  public void setBiografia(String biografia) {
    this.biografia = biografia;
  }

  public String getSenhaHash() {
    return senhaHash;
  }

  public void setSenhaHash(String senhaHash) {
    this.senhaHash = senhaHash;
  }

  public Integer getIdImagemPerfil() {
    return idImagemPerfil;
  }

  public void setIdImagemPerfil(Integer idImagemPerfil) {
    this.idImagemPerfil = idImagemPerfil;
  }

  public StatusUsuario getStatusUsuario() {
    return statusUsuario;
  }

  public void setStatusUsuario(StatusUsuario statusUsuario) {
    this.statusUsuario = statusUsuario;
  }

  public TipoUsuario getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(TipoUsuario tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
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

  public void applyUpdate(UsuarioUpdateDTO dto) {
    if (dto.getEmail() != null) this.email = dto.getEmail();
    if (dto.getCpf() != null) this.cpf = dto.getCpf();
    if (dto.getTelefone() != null) this.telefone = dto.getTelefone();
    if (dto.getTelefone2() != null) this.telefone2 = dto.getTelefone2();
    if (dto.getNome() != null) this.nome = dto.getNome();
    if (dto.getBiografia() != null) this.biografia = dto.getBiografia();
    if (dto.getSenha() != null) this.senhaHash = dto.getSenha();
    if (dto.getIdImagemPerfil() != null) this.idImagemPerfil = dto.getIdImagemPerfil();
    if (dto.getStatusUsuario() != null) this.statusUsuario = dto.getStatusUsuario();
    if (dto.getTipoUsuario() != null) this.tipoUsuario = dto.getTipoUsuario();
    if (dto.getTags() != null) {
      this.tags.clear();
      this.tags.addAll(dto.getTags());
    }
  }
}
