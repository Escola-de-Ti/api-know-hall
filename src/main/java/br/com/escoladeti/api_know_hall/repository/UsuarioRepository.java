package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.projection.usuario.UsuarioRankingProjection;
import br.com.escoladeti.api_know_hall.projection.usuario.UsuarioDetalhesProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, BigInteger> {

  Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findByCpf(String cpf);

  @Query(value = """
    SELECT
        ROW_NUMBER() OVER (ORDER BY u.qntd_xp DESC) as posicao,
        u.id,
        u.nome,
        u.qntd_xp,
        u.nivel
    FROM usuario u
    ORDER BY u.qntd_xp DESC
    LIMIT 50
    """, nativeQuery = true)
  List<UsuarioRankingProjection> findTop50UsuariosPorXp();

  @Query(value = """
    SELECT posicao
    FROM (
        SELECT
            u.id,
            ROW_NUMBER() OVER (ORDER BY u.qntd_xp DESC) as posicao
        FROM usuario u
    ) ranking
    WHERE id = :usuarioId
    """, nativeQuery = true)
  Long findPosicaoNoRanking(@Param("usuarioId") BigInteger usuarioId);

  @Query(value = """
    SELECT COALESCE(SUM(ht.quantidade), 0)
    FROM historico_transacao ht
    WHERE ht.usuario_id = :usuarioId
    AND ht.data_transacao >= CURRENT_DATE - INTERVAL '30 days'
    AND ht.motivo != 'INSCRICAO_WORKSHOP_ALUNO'
    """, nativeQuery = true)
  Integer findXpRecebidoUltimos30Dias(@Param("usuarioId") BigInteger usuarioId);

  @Query(value = """
    SELECT
        u.nome AS nome,
        u.biografia AS biografia,
        u.nivel AS nivel,
        CAST(u.qntd_xp AS BIGINT) AS xp,
        CAST(u.qntd_token AS BIGINT) AS tokens,
        COALESCE(COUNT(DISTINCT p.id), 0) AS qtdPosts,
        COALESCE(COUNT(DISTINCT c.id), 0) AS qtdComentarios,
        COALESCE(COUNT(DISTINCT CASE WHEN v.tipo = 'UP_VOTE' THEN v.id END), 0) AS qtdUpVotes,
        COALESCE(COUNT(DISTINCT CASE WHEN v.tipo = 'SUPER_VOTE' THEN v.id END), 0) AS qtdSuperVotes,
        COALESCE(COUNT(DISTINCT w.id), 0) AS qtdWorkshops,
        img.url AS imagemUrl
    FROM usuario u
    LEFT JOIN imagem img ON img.id = u.id_imagem_perfil
    LEFT JOIN post p ON p.usuario_id = u.id
    LEFT JOIN comentario c ON c.usuario_id = u.id
    LEFT JOIN votos v ON v.usuario_id = u.id
    LEFT JOIN workshop w ON w.id_instrutor = u.id
    WHERE u.id = :usuarioId
    GROUP BY u.id, u.nome, u.biografia, u.nivel, u.qntd_xp, u.qntd_token, img.url
    """, nativeQuery = true)
  Optional<UsuarioDetalhesProjection> findDetalhesUsuarioById(@Param("usuarioId") BigInteger usuarioId);
}
