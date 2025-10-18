package br.com.escoladeti.api_know_hall.entity.conquista;

import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CONQUISTA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conquista {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @Column(name = "tipo_conquista", nullable = false)
  @Enumerated(EnumType.STRING)
  private TipoConquista tipoConquista;

  @Column(name = "campo_validacao", nullable = false, length = 100)
  private String campoValidacao;

  @Column(name = "nome", nullable = false)
  private String nome;

  @Column(name = "descricao", columnDefinition = "TEXT")
  private String descricao;

  @Column(name = "icone_url")
  private String iconeUrl;

//  Relacionamento opcional com Workshop (apenas para CERTIFICADO)
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "workshop_id")
//  private Workshop workshop;

  @OneToMany(mappedBy = "conquista", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("tier ASC")
  private List<ConquistaTier> tiers = new ArrayList<>();

  @OneToMany(mappedBy = "conquista", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UsuarioConquista> usuariosConquistas = new ArrayList<>();

  // Validação customizada
//  @PrePersist
//  @PreUpdate
//  private void validarIntegridadeConquista() {
//    if (tipoConquista == TipoConquista.CERTIFICADO && workshop == null) {
//      throw new IllegalStateException("Conquista do tipo CERTIFICADO deve ter um Workshop associado");
//    }
//    if (tipoConquista == TipoConquista.INSIGNIA && workshop != null) {
//      throw new IllegalStateException("Conquista do tipo INSIGNIA não pode ter Workshop associado");
//    }
//  }

  public void adicionarTier(TierConquista tier, Integer quantidadeNecessaria) {
    ConquistaTier conquistaTier = new ConquistaTier();
    conquistaTier.setConquista(this);
    conquistaTier.setTier(tier);
    conquistaTier.setQuantidadeNecessaria(quantidadeNecessaria);
    this.tiers.add(conquistaTier);
  }
}
