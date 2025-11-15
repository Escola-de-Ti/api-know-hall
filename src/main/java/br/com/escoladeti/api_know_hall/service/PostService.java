package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.ImagemPostDTO;
import br.com.escoladeti.api_know_hall.dto.comentario.ComentarioResponseDTO;
import br.com.escoladeti.api_know_hall.dto.post.*;
import br.com.escoladeti.api_know_hall.dto.tags.TagResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.OrdenacaoTipo;
import br.com.escoladeti.api_know_hall.projection.comentario.ComentarioProjection;
import br.com.escoladeti.api_know_hall.projection.post.PostBuscaProjection;
import br.com.escoladeti.api_know_hall.projection.post.PostFeedProjection;
import br.com.escoladeti.api_know_hall.projection.tag.PostTagProjection;
import br.com.escoladeti.api_know_hall.repository.ComentarioRepository;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import br.com.escoladeti.api_know_hall.repository.TagsRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final UsuarioRepository usuarioRepository;
  private final TagsRepository tagsRepository;
  private final ComentarioRepository comentarioRepository;

  @Transactional
  public PostResponseDTO criarPost(PostCreateDTO dto, String emailUsuario) {
    Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

    Post post = new Post();
    post.setUsuario(usuario);
    post.setTitulo(dto.titulo());
    post.setDescricao(dto.descricao());
    post.setTotalUpVotes(0L);
    post.setMaiorQntdVoto(0L);

    if (dto.tagIds() != null && !dto.tagIds().isEmpty()) {
      List<Tag> tags = tagsRepository.findAllById(dto.tagIds());

      if (tags.size() != dto.tagIds().size()) {
        throw new EntityNotFoundException("Uma ou mais tags não foram encontradas");
      }

      post.setTags(tags);
    } else {
      post.setTags(new ArrayList<>());
    }

    Post postSalvo = postRepository.save(post);
    return mapToResponseDTO(postSalvo);
  }

  @Transactional(readOnly = true)
  public PostResponseDTO buscarPorId(BigInteger id) {
    Post post = postRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));
    return mapToResponseDTO(post);
  }

  @Transactional(readOnly = true)
  public List<PostResponseDTO> listarTodos() {
    return postRepository.findAll().stream()
      .map(this::mapToResponseDTO)
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PostResponseDTO> listarPorUsuario(BigInteger usuarioId) {
    return postRepository.findByUsuarioId(usuarioId).stream()
      .map(this::mapToResponseDTO)
      .collect(Collectors.toList());
  }

  @Transactional
  public PostResponseDTO atualizarPost(BigInteger id, PostUpdateDTO dto) {
    Post post = postRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));

    if (dto.titulo() != null) {
      post.setTitulo(dto.titulo());
    }
    if (dto.descricao() != null) {
      post.setDescricao(dto.descricao());
    }
    if (dto.tagIds() != null) {
      List<Tag> tags = tagsRepository.findAllById(dto.tagIds());

      // Validar se todas as tags foram encontradas
      if (tags.size() != dto.tagIds().size()) {
        throw new EntityNotFoundException("Uma ou mais tags não foram encontradas");
      }

      post.setTags(tags);
    }

    Post postAtualizado = postRepository.save(post);
    return mapToResponseDTO(postAtualizado);
  }

  @Transactional
  public void deletarPost(BigInteger id) {
    if (!postRepository.existsById(id)) {
      throw new EntityNotFoundException("Post não encontrado");
    }
    postRepository.deleteById(id);
  }

  private PostResponseDTO mapToResponseDTO(Post post) {
    List<TagResponseDTO> tagDTOs = post.getTags().stream()
      .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()))
      .collect(Collectors.toList());

    List<ImagemPostDTO> imagemDTOs = post.getImagens().stream()
      .map(ImagemPostDTO::fromEntity)
      .toList();

    return new PostResponseDTO(
      post.getId(),
      post.getUsuario().getId(),
      post.getUsuario().getNome(),
      post.getTitulo(),
      post.getDescricao(),
      post.getTotalUpVotes(),
      tagDTOs,
      post.getDataCriacao(),
      imagemDTOs
    );
  }

  @Transactional(readOnly = true)
  public FeedResponseDTO getFeed(FeedRequestDTO request) {
    Long usuarioIdLong = request.usuarioId().longValue();
    Long lastPostIdLong = request.lastPostId() != null ? request.lastPostId().longValue() : null;

    Integer fetchSize = request.pageSize() + 1;

    String filterTagIds = convertTagIdsToCommaSeparatedString(request.tagIds());
    Integer filterTagCount = request.tagIds() != null ? request.tagIds().size() : null;
    String tagOperador = request.tagOperador() != null ? request.tagOperador().name() : null;

    String dataInicio = request.dataInicio() != null ? request.dataInicio().toString() : null;
    String dataFim = request.dataFim() != null ? request.dataFim().toString() : null;

    List<PostFeedProjection> results = postRepository.findFeedPosts(
      usuarioIdLong,
      request.lastScore(),
      lastPostIdLong,
      fetchSize,
      filterTagIds,
      tagOperador,
      filterTagCount,
      dataInicio,
      dataFim
    );

    boolean hasMore = results.size() > request.pageSize();

    if (hasMore) {
      results = results.subList(0, request.pageSize());
    }

    Map<BigInteger, List<TagResponseDTO>> tagsByPostId = fetchTagsForPosts(results);

    List<PostFeedDTO> posts = results.stream()
      .map(projection -> mapToPostFeedDTO(projection, tagsByPostId))
      .collect(Collectors.toList());

    BigInteger lastPostId = null;
    Double lastScore = null;

    if (!posts.isEmpty()) {
      PostFeedDTO lastPost = posts.get(posts.size() - 1);
      lastPostId = lastPost.id();
      lastScore = lastPost.relevanceScore();
    }

    return new FeedResponseDTO(posts, hasMore, lastPostId, lastScore);
  }

  private Map<BigInteger, List<TagResponseDTO>> fetchTagsForPosts(List<PostFeedProjection> projections) {
    if (projections.isEmpty()) {
      return new HashMap<>();
    }

    List<BigInteger> postIds = projections.stream()
      .map(PostFeedProjection::getId)
      .collect(Collectors.toList());

    List<PostTagProjection> tagsData = postRepository.findTagsByPostIds(postIds);

    return tagsData.stream()
      .collect(Collectors.groupingBy(
        PostTagProjection::getPostId,
        Collectors.mapping(
          projection -> new TagResponseDTO(
            projection.getTagId(),
            projection.getTagName()
          ),
          Collectors.toList()
        )
      ));
  }

  private PostFeedDTO mapToPostFeedDTO(
    PostFeedProjection projection,
    Map<BigInteger, List<TagResponseDTO>> tagsByPostId) {

    List<TagResponseDTO> tags = tagsByPostId.getOrDefault(
      projection.getId(),
      new ArrayList<>()
    );

    return new PostFeedDTO(
      projection.getId(),
      projection.getUsuarioId(),
      projection.getUsuarioNome(),
      projection.getTitulo(),
      projection.getDescricao(),
      projection.getTotalUpVotes(),
      tags,
      projection.getDataCriacao(),
      projection.getRelevanceScore(),
      projection.getTagsEmComum(),
      projection.getJaVotou()
    );
  }

  @Transactional(readOnly = true)
  public PostBuscaResponseDTO buscarPosts(PostBuscaRequestDTO request) {
    Integer fetchSize = request.pageSize() + 1;

    String filterTagIds = convertTagIdsToCommaSeparatedString(request.tagIds());
    Integer filterTagCount = request.tagIds() != null ? request.tagIds().size() : null;
    Long lastPostIdLong = request.lastPostId() != null ? request.lastPostId().longValue() : null;

    String dataInicio = request.dataInicio() != null ? request.dataInicio().toString() : null;
    String dataFim = request.dataFim() != null ? request.dataFim().toString() : null;

    String termo = request.termo() != null && !request.termo().isBlank()
      ? request.termo().trim()
      : null;

    List<PostBuscaProjection> results = postRepository.buscarComFiltros(
      filterTagIds,
      request.tagOperador().name(),
      filterTagCount,
      dataInicio,
      dataFim,
      request.ordenacao().name(),
      request.direcao().name(),
      request.lastValue(),
      lastPostIdLong,
      fetchSize,
      termo
    );

    boolean hasMore = results.size() > request.pageSize();

    if (hasMore) {
      results = results.subList(0, request.pageSize());
    }

    List<PostBuscaItemDTO> posts = results.stream()
      .map(this::mapToPostBuscaItemDTO)
      .collect(Collectors.toList());

    BigInteger lastPostId = null;
    Long lastValue = null;

    if (!posts.isEmpty()) {
      PostBuscaItemDTO lastPost = posts.get(posts.size() - 1);
      lastPostId = lastPost.id();

      if (request.ordenacao() == OrdenacaoTipo.VOTOS) {
        lastValue = lastPost.totalUpVotes();
      } else {
        lastValue = lastPost.dataCriacao().getTime() / 1000;
      }
    }

    return new PostBuscaResponseDTO(posts, hasMore, lastPostId, lastValue);
  }

  private String convertTagIdsToCommaSeparatedString(List<BigInteger> tagIds) {
    if (tagIds == null || tagIds.isEmpty()) {
      return null;
    }
    return tagIds.stream()
      .map(String::valueOf)
      .collect(Collectors.joining(","));
  }

  private PostBuscaItemDTO mapToPostBuscaItemDTO(PostBuscaProjection projection) {
    List<TagResponseDTO> tags = postRepository.findById(projection.getId())
      .map(post -> post.getTags().stream()
        .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()))
        .collect(Collectors.toList()))
      .orElse(new ArrayList<>());

    return new PostBuscaItemDTO(
      projection.getId(),
      projection.getUsuarioId(),
      projection.getUsuarioNome(),
      projection.getTitulo(),
      projection.getDescricao(),
      projection.getTotalUpVotes(),
      tags,
      projection.getDataCriacao()
    );
  }

  public Usuario findUserByPrincipal(String email) {
    return usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
  }

  @Transactional(readOnly = true)
  public PostDetalhesDTO buscarDetalhesDoPost(BigInteger postId, Integer pageSize, String usuarioEmail) {
    Usuario usuario = findUserByPrincipal(usuarioEmail);

    Post post = postRepository.findById(postId)
      .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));

    List<ImagemPostDTO> imagemDTOs = post.getImagens().stream()
      .map(ImagemPostDTO::fromEntity)
      .toList();

    List<TagResponseDTO> tagDTOs = post.getTags().stream()
      .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()))
      .collect(Collectors.toList());

    Integer fetchSize = pageSize + 1;
    List<ComentarioProjection> comentariosResults = comentarioRepository.findComentariosByPostId(
      postId,
      null,
      fetchSize,
      usuario.getId()
    );

    boolean hasMoreComentarios = comentariosResults.size() > pageSize;

    if (hasMoreComentarios) {
      comentariosResults = comentariosResults.subList(0, pageSize);
    }

    List<ComentarioResponseDTO> comentarios = comentariosResults.stream()
      .map(this::mapComentarioProjectionToDTO)
      .collect(Collectors.toList());

    Boolean jaVotou = this.postRepository.getJaVotouByPostIdAndUsuarioId(postId, usuario.getId());

    return new PostDetalhesDTO(
      post.getId(),
      post.getUsuario().getId(),
      post.getUsuario().getNome(),
      post.getTitulo(),
      post.getDescricao(),
      post.getTotalUpVotes(),
      tagDTOs,
      post.getDataCriacao(),
      comentarios,
      hasMoreComentarios,
      jaVotou,
      imagemDTOs
    );
  }

  private ComentarioResponseDTO mapComentarioProjectionToDTO(ComentarioProjection projection) {
    return new ComentarioResponseDTO(
      projection.getId(),
      projection.getPostId(),
      projection.getUsuarioId(),
      projection.getUsuarioNome(),
      projection.getTexto(),
      projection.getTotalUpVotes(),
      projection.getTotalSuperVotes(),
      projection.getComentarioPaiId(),
      projection.getDataCriacao(),
      projection.getJaVotou()
    );
  }

  @Transactional
  public void adicionaAtualizarImagemPost(Imagem imagem, Integer ordemImagem, BigInteger postId) {
    Post post = postRepository.findById(postId)
      .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));
    post.addImagem(imagem, ordemImagem);
    postRepository.save(post);
  }

  @Transactional
  public void removerImagemPost(BigInteger postId, BigInteger imagemId) {
    Post post = postRepository.findById(postId)
      .orElseThrow(() -> new EntityNotFoundException("Post não encontrado"));

    post.getImagens().removeIf(imgPost -> imgPost.getImagem().getId().equals(imagemId));
    postRepository.save(post);
  }
}
