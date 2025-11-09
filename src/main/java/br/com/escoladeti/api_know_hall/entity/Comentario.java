package br.com.escoladeti.api_know_hall.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "COMENTARIO")
public class Comentario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private BigInteger id;

  @Column(name = "texto")
  private String texto;

  @Column(name = "total_up_votes")
  private Long totalUpVotes;

  @Column(name = "total_super_votes")
  private Long totalSuperVotes;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comentario_pai_id")
  private Comentario comentarioPai;

  @OneToMany(mappedBy = "comentarioPai", cascade = CascadeType.ALL)
  private List<Comentario> respostas = new ArrayList<>();

  @Column(name = "data_criacao", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp dataCriacao;

  @Column(name = "resposta_destaque", nullable = false)
  private Boolean respostaDestaque = false;

  @Column(name = "maior_qntd_voto")
  private Long maiorQntdVoto;
}
