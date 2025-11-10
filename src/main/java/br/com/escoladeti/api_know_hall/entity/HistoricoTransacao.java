package br.com.escoladeti.api_know_hall.entity;

import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
@Table(name = "HISTORICO_TRANSACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoTransacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(name = "quantidade", nullable = false)
  private Long quantidade;

  @Enumerated(EnumType.STRING)
  @Column(name = "motivo", nullable = false, length = 50)
  private MotivoTransacao motivo;

  @Column(name = "descricao", length = 500)
  private String descricao;

  @Column(name = "data_transacao", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp dataTransacao;
}
