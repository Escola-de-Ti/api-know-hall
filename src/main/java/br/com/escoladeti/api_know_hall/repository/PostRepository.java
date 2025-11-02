package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.projection.post.PostBuscaProjection;
import br.com.escoladeti.api_know_hall.projection.post.PostFeedProjection;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.projection.tag.PostTagProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, BigInteger> {
  List<Post> findByUsuarioId(BigInteger usuarioId);

  @Query(value = """
        WITH user_tags AS (
            SELECT tag_id
            FROM usuario_tags
            WHERE usuario_id = :usuarioId
        ),
        post_tag_counts AS (
            SELECT
                pt.post_id,
                COUNT(pt.tag_id) AS tags_em_comum
            FROM post_tags pt
            JOIN user_tags ut ON pt.tag_id = ut.tag_id
            GROUP BY pt.post_id
        ),
        post_base AS (
            SELECT
                p.id,
                p.usuario_id,
                p.titulo,
                p.descricao,
                p.total_up_votes,
                p.data_criacao,
                COALESCE(ptc.tags_em_comum, 0) AS tags_em_comum,
                (CURRENT_DATE - p.data_criacao::date) AS age_days
            FROM post p
            LEFT JOIN post_tag_counts ptc ON ptc.post_id = p.id
            WHERE p.data_criacao >= CURRENT_TIMESTAMP - INTERVAL '180 days'
                AND (CAST(:dataInicio AS DATE) IS NULL OR p.data_criacao::date >= CAST(:dataInicio AS DATE))
                AND (CAST(:dataFim AS DATE) IS NULL OR p.data_criacao::date <= CAST(:dataFim AS DATE))
                AND (
                    :filterTagIds IS NULL
                    OR (
                        :tagOperador = 'OR' AND EXISTS (
                            SELECT 1 FROM post_tags pt2
                            WHERE pt2.post_id = p.id
                            AND pt2.tag_id = ANY(string_to_array(:filterTagIds, ',')::bigint[])
                        )
                    )
                    OR (
                        :tagOperador = 'AND' AND (
                            SELECT COUNT(DISTINCT pt2.tag_id)
                            FROM post_tags pt2
                            WHERE pt2.post_id = p.id
                            AND pt2.tag_id = ANY(string_to_array(:filterTagIds, ',')::bigint[])
                        ) = :filterTagCount
                    )
                )
        ),
        post_scores AS (
            SELECT
                id,
                usuario_id,
                titulo,
                descricao,
                total_up_votes,
                data_criacao,
                tags_em_comum,
                (
                    (COALESCE(total_up_votes, 0) * 0.3) +
                    (tags_em_comum * 10.0) +
                    (CASE
                        WHEN age_days <= 7  THEN 20.0
                        WHEN age_days <= 30 THEN 10.0
                        WHEN age_days <= 90 THEN 4.0
                        ELSE 2.0
                    END)
                )::double precision AS relevance_score
            FROM post_base
        ),
        paginated_posts AS (
            SELECT *
            FROM post_scores
            WHERE :lastScore IS NULL
               OR (
                   relevance_score < :lastScore
                   OR (relevance_score = :lastScore AND id < :lastPostId)
               )
            ORDER BY relevance_score DESC, id DESC
            LIMIT :pageSize
        )
        SELECT
            pp.id AS id,
            pp.usuario_id AS usuarioId,
            u.nome AS usuarioNome,
            pp.titulo AS titulo,
            pp.descricao AS descricao,
            pp.total_up_votes AS totalUpVotes,
            pp.data_criacao AS dataCriacao,
            pp.relevance_score AS relevanceScore,
            pp.tags_em_comum AS tagsEmComum
        FROM paginated_posts pp
        JOIN usuario u ON pp.usuario_id = u.id
        ORDER BY pp.relevance_score DESC, pp.id DESC
        """, nativeQuery = true)
  List<PostFeedProjection> findFeedPosts(
    @Param("usuarioId") Long usuarioId,
    @Param("lastScore") Double lastScore,
    @Param("lastPostId") Long lastPostId,
    @Param("pageSize") Integer pageSize,
    @Param("filterTagIds") String filterTagIds,
    @Param("tagOperador") String tagOperador,
    @Param("filterTagCount") Integer filterTagCount,
    @Param("dataInicio") String dataInicio,
    @Param("dataFim") String dataFim
  );

  @Query(value = """
    WITH filtered_posts AS (
        SELECT
            p.id,
            p.usuario_id,
            p.titulo,
            p.descricao,
            p.total_up_votes,
            p.data_criacao,
            EXTRACT(EPOCH FROM p.data_criacao)::bigint AS data_timestamp
        FROM post p
        WHERE 1=1
            AND (
                :termo IS NULL
                OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))
            )
            AND (CAST(:dataInicio AS DATE) IS NULL OR p.data_criacao::date >= CAST(:dataInicio AS DATE))
            AND (CAST(:dataFim AS DATE) IS NULL OR p.data_criacao::date <= CAST(:dataFim AS DATE))
            AND (
                :filterTagIds IS NULL
                OR (
                    :tagOperador = 'OR' AND EXISTS (
                        SELECT 1 FROM post_tags pt
                        WHERE pt.post_id = p.id
                        AND pt.tag_id = ANY(string_to_array(:filterTagIds, ',')::bigint[])
                    )
                )
                OR (
                    :tagOperador = 'AND' AND (
                        SELECT COUNT(DISTINCT pt.tag_id)
                        FROM post_tags pt
                        WHERE pt.post_id = p.id
                        AND pt.tag_id = ANY(string_to_array(:filterTagIds, ',')::bigint[])
                    ) = :filterTagCount
                )
            )
            AND (
                :lastValue IS NULL
                OR (
                    (:ordenacao = 'VOTOS' AND :direcao = 'DESC' AND
                        (p.total_up_votes < :lastValue OR (p.total_up_votes = :lastValue AND p.id < :lastPostId))
                    )
                    OR (:ordenacao = 'VOTOS' AND :direcao = 'ASC' AND
                        (p.total_up_votes > :lastValue OR (p.total_up_votes = :lastValue AND p.id > :lastPostId))
                    )
                    OR (:ordenacao = 'DATA' AND :direcao = 'DESC' AND
                        (EXTRACT(EPOCH FROM p.data_criacao)::bigint < :lastValue OR
                         (EXTRACT(EPOCH FROM p.data_criacao)::bigint = :lastValue AND p.id < :lastPostId))
                    )
                    OR (:ordenacao = 'DATA' AND :direcao = 'ASC' AND
                        (EXTRACT(EPOCH FROM p.data_criacao)::bigint > :lastValue OR
                         (EXTRACT(EPOCH FROM p.data_criacao)::bigint = :lastValue AND p.id > :lastPostId))
                    )
                )
            )
    )
    SELECT
        fp.id AS id,
        fp.usuario_id AS usuarioId,
        u.nome AS usuarioNome,
        fp.titulo AS titulo,
        fp.descricao AS descricao,
        fp.total_up_votes AS totalUpVotes,
        fp.data_criacao AS dataCriacao
    FROM filtered_posts fp
    JOIN usuario u ON fp.usuario_id = u.id
    ORDER BY
        CASE
            WHEN :ordenacao = 'VOTOS' AND :direcao = 'DESC' THEN fp.total_up_votes
        END DESC,
        CASE
            WHEN :ordenacao = 'VOTOS' AND :direcao = 'ASC' THEN fp.total_up_votes
        END ASC,
        CASE
            WHEN :ordenacao = 'DATA' AND :direcao = 'DESC' THEN fp.data_criacao
        END DESC,
        CASE
            WHEN :ordenacao = 'DATA' AND :direcao = 'ASC' THEN fp.data_criacao
        END ASC,
        CASE WHEN :direcao = 'DESC' THEN fp.id END DESC,
        CASE WHEN :direcao = 'ASC' THEN fp.id END ASC
    LIMIT :pageSize
    """, nativeQuery = true)
  List<PostBuscaProjection> buscarComFiltros(
    @Param("filterTagIds") String filterTagIds,
    @Param("tagOperador") String tagOperador,
    @Param("filterTagCount") Integer filterTagCount,
    @Param("dataInicio") String dataInicio,
    @Param("dataFim") String dataFim,
    @Param("ordenacao") String ordenacao,
    @Param("direcao") String direcao,
    @Param("lastValue") Long lastValue,
    @Param("lastPostId") Long lastPostId,
    @Param("pageSize") Integer pageSize,
    @Param("termo") String termo
  );

  @Query(value = """
        SELECT
            pt.post_id as postId,
            t.id as tagId,
            t.name as tagName
        FROM post_tags pt
        JOIN tags t ON pt.tag_id = t.id
        WHERE pt.post_id IN :postIds
        ORDER BY pt.post_id, t.name
        """, nativeQuery = true)
  List<PostTagProjection> findTagsByPostIds(@Param("postIds") List<BigInteger> postIds);
}
