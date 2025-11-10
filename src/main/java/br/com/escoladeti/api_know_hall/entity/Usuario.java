package br.com.escoladeti.api_know_hall.entity;

import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.usuario.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.Optional;

@Entity
@Table(name = "USUARIO")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

  @Column(name = "biografia")
  private String biografia;

  @Column(name = "senha_hash", nullable = false)
  private String senhaHash;

  @Column(name = "qntd_token")
  private Long qntdToken;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "id_imagem_perfil", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_usuario_imagem"))
  private Imagem imagemPerfil;

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

  public void setIdImagemPerfil(BigInteger idImagemPerfil) {
    if (idImagemPerfil == null) {
      this.imagemPerfil = null;
    } else {
      Imagem img = new Imagem();
      img.setId(idImagemPerfil);
      this.imagemPerfil = img;
    }
  }

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UsuarioConquista> conquistas = new ArrayList<>();

  @OneToMany(mappedBy = "instrutor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Workshop> workshops = new ArrayList<>();

  @Column(name = "qntd_token")
  private Long qntdToken;

  @Column(name = "qntd_xp")
  private Long qntdXp;

  public void adicionarConquista(ConquistaTier conquistaTier, Integer progressoAtual) {
    UsuarioConquista uc = new UsuarioConquista();
    uc.setUsuario(this);
    uc.setConquistaTier(conquistaTier);
    uc.setDataObtencao(LocalDateTime.now());
    uc.setProgressoAtual(progressoAtual);

    if (!this.conquistas.contains(uc)) {
      this.conquistas.add(uc);
    }
  }

  public boolean possuiConquistaTier(BigInteger conquistaId, TierConquista tier) {
    return conquistas.stream()
      .anyMatch(uc -> uc.getConquista().getId().equals(conquistaId)
        && uc.getConquistaTier().getTier().equals(tier));
  }

  public Optional<TierConquista> getMaiorTierConquistado(BigInteger conquistaId) {
    return conquistas.stream()
      .filter(uc -> uc.getConquista().getId().equals(conquistaId))
      .map(uc -> uc.getConquistaTier().getTier())
      .max(Comparator.comparingInt(TierConquista::getNivel));
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
    this.statusUsuario = StatusUsuario.ATIVO;
    this.tipoUsuario = dto.getTipoUsuario();
    this.tags = dto.getTags();
    this.qntdToken = 0L;
    this.qntdXp = 0L;
  }

  public void applyUpdate(UsuarioUpdateDTO dto) {
    if (dto.getEmail() != null) this.email = dto.getEmail();
    if (dto.getCpf() != null) this.cpf = dto.getCpf();
    if (dto.getTelefone() != null) this.telefone = dto.getTelefone();
    if (dto.getTelefone2() != null) this.telefone2 = dto.getTelefone2();
    if (dto.getNome() != null) this.nome = dto.getNome();
    if (dto.getBiografia() != null) this.biografia = dto.getBiografia();
    if (dto.getSenha() != null) this.senhaHash = dto.getSenha();
    if (dto.getStatusUsuario() != null) this.statusUsuario = dto.getStatusUsuario();
    if (dto.getTipoUsuario() != null) this.tipoUsuario = dto.getTipoUsuario();
    if (dto.getTags() != null) {
      this.tags.clear();
      this.tags.addAll(dto.getTags());
    }
  }
}
