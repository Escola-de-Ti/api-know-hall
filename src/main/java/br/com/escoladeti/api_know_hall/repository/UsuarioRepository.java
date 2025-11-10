package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.projection.usuario.UsuarioRankingProjection;
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
}
