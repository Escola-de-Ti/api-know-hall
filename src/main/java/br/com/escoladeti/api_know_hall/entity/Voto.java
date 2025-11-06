package br.com.escoladeti.api_know_hall.entity;

import br.com.escoladeti.api_know_hall.enums.TipoVoto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
@Table(name = "VOTOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private BigInteger id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comentario_id")
  private Comentario comentario;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TipoVoto tipo;

  @Column(name = "data_criacao", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp dataCriacao;

  @PrePersist
  @PreUpdate
  public void validateVoto() {
    if ((post == null && comentario == null) || (post != null && comentario != null)) {
      throw new IllegalStateException("Voto deve ser associado a um Post OU a um Comentário, nunca ambos ou nenhum");
    }
  }
}
