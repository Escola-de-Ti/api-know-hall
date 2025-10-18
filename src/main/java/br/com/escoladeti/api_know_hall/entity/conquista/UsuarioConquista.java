package br.com.escoladeti.api_know_hall.entity.conquista;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(
  name = "USUARIO_CONQUISTA",
  uniqueConstraints = @UniqueConstraint(
    name = "uk_usuario_conquista_tier",
    columnNames = {"usuario_id", "conquista_tier_id"}
  )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioConquista {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  // Método auxiliar para estabelecer relacionamento bidirecional
  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conquista_id", nullable = false)
  private Conquista conquista;

  // NOVO: Relacionamento com o tier específico conquistado
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conquista_tier_id", nullable = false)
  private ConquistaTier conquistaTier;

  @Column(name = "data_obtencao", nullable = false)
  private LocalDateTime dataObtencao;

  @Column(name = "progresso_atual")
  private Integer progressoAtual;

  @PrePersist
  protected void onCreate() {
    if (dataObtencao == null) {
      dataObtencao = LocalDateTime.now();
    }
  }

  public void setConquista(Conquista conquista) {
    this.conquista = conquista;
    if (conquista != null && !conquista.getUsuariosConquistas().contains(this)) {
      conquista.getUsuariosConquistas().add(this);
    }
  }

  public void setConquistaTier(ConquistaTier conquistaTier) {
    this.conquistaTier = conquistaTier;
    // Automaticamente seta a conquista também
    if (conquistaTier != null) {
      this.conquista = conquistaTier.getConquista();
    }
  }
}
