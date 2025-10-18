package br.com.escoladeti.api_know_hall.entity.conquista;

import br.com.escoladeti.api_know_hall.enums.TierConquista;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
  name = "CONQUISTA_TIER",
  uniqueConstraints = @UniqueConstraint(
    name = "uk_conquista_tier",
    columnNames = {"conquista_id", "tier"}
  )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConquistaTier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conquista_id", nullable = false)
  private Conquista conquista;

  @Column(name = "tier", nullable = false)
  @Enumerated(EnumType.STRING)
  private TierConquista tier;

  @Column(name = "quantidade_necessaria", nullable = false)
  private Integer quantidadeNecessaria;

  @Column(name = "descricao_tier")
  private String descricaoTier;

  @OneToMany(mappedBy = "conquistaTier", cascade = CascadeType.ALL)
  private List<UsuarioConquista> usuariosConquistas = new ArrayList<>();
}
