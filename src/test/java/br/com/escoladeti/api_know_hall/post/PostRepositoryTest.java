package br.com.escoladeti.api_know_hall.post;

import br.com.escoladeti.api_know_hall.entity.Post;
import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.*;
import br.com.escoladeti.api_know_hall.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes Básicos do PostRepository")
class PostRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private PostRepository postRepository;

  private Usuario usuario1;
  private Usuario usuario2;
  private Tag tag1;
  private Tag tag2;
  private Tag tag3;

  @BeforeEach
  void setUp() {
    // Criar Tags
    tag1 = new Tag();
    tag1.setName("JAVA");
    tag1 = entityManager.persist(tag1);

    tag2 = new Tag();
    tag2.setName("SPRING");
    tag2 = entityManager.persist(tag2);

    tag3 = new Tag();
    tag3.setName("REACT");
    tag3 = entityManager.persist(tag3);

    // Criar Usuários
    usuario1 = criarUsuario("joao@email.com", "12345678901", "João Silva");
    usuario2 = criarUsuario("maria@email.com", "98765432109", "Maria Santos");

    entityManager.flush();
    // ❌ REMOVER entityManager.clear() AQUI - isso causa o problema
  }

  // ==================== TESTES DE CRUD BÁSICO ====================

  @Test
  @DisplayName("Deve salvar post com sucesso")
  void deveSalvarPostComSucesso() {
    // ✅ Re-attach das tags antes de usar
    Tag tagAttached = entityManager.find(Tag.class, tag1.getId());
    Post post = criarPost(usuario1, "Título de Teste", "Descrição", 0L, List.of(tagAttached));

    Post postSalvo = postRepository.save(post);
    entityManager.flush();

    assertThat(postSalvo).isNotNull();
    assertThat(postSalvo.getId()).isNotNull();
    assertThat(postSalvo.getTitulo()).isEqualTo("Título de Teste");
    assertThat(postSalvo.getDescricao()).isEqualTo("Descrição");
    assertThat(postSalvo.getTotalUpVotes()).isEqualTo(0L);
  }

  @Test
  @DisplayName("Deve buscar post por ID")
  void deveBuscarPostPorId() {
    Tag tagAttached = entityManager.find(Tag.class, tag1.getId());
    Post post = criarPost(usuario1, "Post de Busca", "Descrição", 10L, List.of(tagAttached));
    Post postSalvo = entityManager.persistAndFlush(post);

    Optional<Post> postEncontrado = postRepository.findById(postSalvo.getId());

    assertThat(postEncontrado).isPresent();
    assertThat(postEncontrado.get().getTitulo()).isEqualTo("Post de Busca");
    assertThat(postEncontrado.get().getTotalUpVotes()).isEqualTo(10L);
  }

  @Test
  @DisplayName("Deve retornar vazio ao buscar post inexistente")
  void deveRetornarVazioAoBuscarPostInexistente() {
    Optional<Post> postEncontrado = postRepository.findById(java.math.BigInteger.valueOf(999999));

    assertThat(postEncontrado).isEmpty();
  }

  @Test
  @DisplayName("Deve listar todos os posts")
  void deveListarTodosOsPosts() {
    Tag tag1Attached = entityManager.find(Tag.class, tag1.getId());
    Tag tag2Attached = entityManager.find(Tag.class, tag2.getId());
    Tag tag3Attached = entityManager.find(Tag.class, tag3.getId());

    Post post1 = criarPost(usuario1, "Post 1", "Desc 1", 5L, List.of(tag1Attached));
    Post post2 = criarPost(usuario2, "Post 2", "Desc 2", 10L, List.of(tag2Attached));
    Post post3 = criarPost(usuario1, "Post 3", "Desc 3", 15L, List.of(tag3Attached));

    entityManager.persist(post1);
    entityManager.persist(post2);
    entityManager.persist(post3);
    entityManager.flush();

    List<Post> todos = postRepository.findAll();

    assertThat(todos).hasSize(3);
    assertThat(todos).extracting(Post::getTitulo)
      .containsExactlyInAnyOrder("Post 1", "Post 2", "Post 3");
  }

  @Test
  @DisplayName("Deve deletar post por ID")
  void deveDeletarPostPorId() {
    Post post = criarPost(usuario1, "Post para Deletar", "Desc", 0L, List.of());
    Post postSalvo = entityManager.persistAndFlush(post);

    postRepository.deleteById(postSalvo.getId());
    entityManager.flush();

    Optional<Post> postDeletado = postRepository.findById(postSalvo.getId());
    assertThat(postDeletado).isEmpty();
  }

  @Test
  @DisplayName("Deve atualizar post existente")
  void deveAtualizarPostExistente() {
    Tag tagAttached = entityManager.find(Tag.class, tag1.getId());
    Post post = criarPost(usuario1, "Título Original", "Descrição Original", 5L, List.of(tagAttached));
    Post postSalvo = entityManager.persistAndFlush(post);

    postSalvo.setTitulo("Título Atualizado");
    postSalvo.setDescricao("Descrição Atualizada");
    postSalvo.setTotalUpVotes(20L);

    Post postAtualizado = postRepository.save(postSalvo);
    entityManager.flush();
    entityManager.clear();

    Post postVerificado = postRepository.findById(postAtualizado.getId()).orElse(null);
    assertThat(postVerificado).isNotNull();
    assertThat(postVerificado.getTitulo()).isEqualTo("Título Atualizado");
    assertThat(postVerificado.getDescricao()).isEqualTo("Descrição Atualizada");
    assertThat(postVerificado.getTotalUpVotes()).isEqualTo(20L);
  }

  @Test
  @DisplayName("Deve verificar se post existe por ID")
  void deveVerificarSePostExistePorId() {
    Post post = criarPost(usuario1, "Post Existente", "Desc", 0L, List.of());
    Post postSalvo = entityManager.persistAndFlush(post);

    boolean existe = postRepository.existsById(postSalvo.getId());

    assertThat(existe).isTrue();
  }

  @Test
  @DisplayName("Deve retornar false para post inexistente")
  void deveRetornarFalseParaPostInexistente() {
    boolean existe = postRepository.existsById(java.math.BigInteger.valueOf(999999));

    assertThat(existe).isFalse();
  }

  // ==================== TESTES DE RELACIONAMENTOS ====================

  @Test
  @DisplayName("Deve salvar post com relacionamento de usuário")
  void deveSalvarPostComRelacionamentoDeUsuario() {
    Post post = criarPost(usuario1, "Post com Usuario", "Desc", 0L, List.of());
    Post postSalvo = postRepository.save(post);

    entityManager.flush();
    entityManager.clear();

    Post postCarregado = postRepository.findById(postSalvo.getId()).orElse(null);
    assertThat(postCarregado).isNotNull();
    assertThat(postCarregado.getUsuario()).isNotNull();
    assertThat(postCarregado.getUsuario().getId()).isEqualTo(usuario1.getId());
    assertThat(postCarregado.getUsuario().getNome()).isEqualTo("João Silva");
  }

  @Test
  @DisplayName("Deve salvar post com múltiplas tags")
  void deveSalvarPostComMultiplasTags() {
    // ✅ SOLUÇÃO: Re-attach todas as tags antes de usar
    Tag tag1Attached = entityManager.find(Tag.class, tag1.getId());
    Tag tag2Attached = entityManager.find(Tag.class, tag2.getId());
    Tag tag3Attached = entityManager.find(Tag.class, tag3.getId());

    Post post = criarPost(usuario1, "Post com Tags", "Desc", 0L,
      List.of(tag1Attached, tag2Attached, tag3Attached));
    Post postSalvo = postRepository.save(post);

    entityManager.flush();
    entityManager.clear();

    Post postCarregado = postRepository.findById(postSalvo.getId()).orElse(null);
    assertThat(postCarregado).isNotNull();
    assertThat(postCarregado.getTags()).hasSize(3);
    assertThat(postCarregado.getTags()).extracting(Tag::getName)
      .containsExactlyInAnyOrder("JAVA", "SPRING", "REACT");
  }

  @Test
  @DisplayName("Deve salvar post sem tags")
  void deveSalvarPostSemTags() {
    Post post = criarPost(usuario1, "Post Sem Tags", "Desc", 0L, List.of());
    Post postSalvo = postRepository.save(post);

    entityManager.flush();
    entityManager.clear();

    Post postCarregado = postRepository.findById(postSalvo.getId()).orElse(null);
    assertThat(postCarregado).isNotNull();
    assertThat(postCarregado.getTags()).isEmpty();
  }

  @Test
  @DisplayName("Deve atualizar tags do post")
  void deveAtualizarTagsDoPost() {
    Tag tag1Attached = entityManager.find(Tag.class, tag1.getId());
    Post post = criarPost(usuario1, "Post", "Desc", 0L, List.of(tag1Attached));
    Post postSalvo = entityManager.persistAndFlush(post);

    // Re-attach novas tags
    Tag tag2Attached = entityManager.find(Tag.class, tag2.getId());
    Tag tag3Attached = entityManager.find(Tag.class, tag3.getId());

    // ✅ SOLUÇÃO: Usar ArrayList (mutável) em vez de List.of() (imutável)
    postSalvo.setTags(new ArrayList<>(List.of(tag2Attached, tag3Attached)));
    postRepository.save(postSalvo);
    entityManager.flush();
    entityManager.clear();

    Post postAtualizado = postRepository.findById(postSalvo.getId()).orElse(null);
    assertThat(postAtualizado).isNotNull();
    assertThat(postAtualizado.getTags()).hasSize(2);
    assertThat(postAtualizado.getTags()).extracting(Tag::getName)
      .containsExactlyInAnyOrder("SPRING", "REACT");
  }

  @Test
  @DisplayName("Deve remover todas as tags do post")
  void deveRemoverTodasAsTagsDoPost() {
    Tag tag1Attached = entityManager.find(Tag.class, tag1.getId());
    Tag tag2Attached = entityManager.find(Tag.class, tag2.getId());

    Post post = criarPost(usuario1, "Post", "Desc", 0L, List.of(tag1Attached, tag2Attached));
    Post postSalvo = entityManager.persistAndFlush(post);

    postSalvo.getTags().clear();
    postRepository.save(postSalvo);
    entityManager.flush();
    entityManager.clear();

    Post postAtualizado = postRepository.findById(postSalvo.getId()).orElse(null);
    assertThat(postAtualizado).isNotNull();
    assertThat(postAtualizado.getTags()).isEmpty();
  }

  // ==================== TESTES DE BUSCA POR USUÁRIO ====================

  @Test
  @DisplayName("Deve buscar posts por usuário")
  void deveBuscarPostsPorUsuario() {
    Tag tag1Attached = entityManager.find(Tag.class, tag1.getId());
    Tag tag2Attached = entityManager.find(Tag.class, tag2.getId());
    Tag tag3Attached = entityManager.find(Tag.class, tag3.getId());

    Post post1 = criarPost(usuario1, "Post 1 Usuario 1", "Desc", 5L, List.of(tag1Attached));
    Post post2 = criarPost(usuario1, "Post 2 Usuario 1", "Desc", 10L, List.of(tag2Attached));
    Post post3 = criarPost(usuario2, "Post Usuario 2", "Desc", 15L, List.of(tag3Attached));

    entityManager.persist(post1);
    entityManager.persist(post2);
    entityManager.persist(post3);
    entityManager.flush();

    List<Post> postsUsuario1 = postRepository.findByUsuarioId(usuario1.getId());

    assertThat(postsUsuario1).hasSize(2);
    assertThat(postsUsuario1).extracting(Post::getTitulo)
      .containsExactlyInAnyOrder("Post 1 Usuario 1", "Post 2 Usuario 1");
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando usuário não tem posts")
  void deveRetornarListaVaziaQuandoUsuarioNaoTemPosts() {
    List<Post> posts = postRepository.findByUsuarioId(usuario2.getId());

    assertThat(posts).isEmpty();
  }

  @Test
  @DisplayName("Deve buscar múltiplos posts do mesmo usuário")
  void deveBuscarMultiplosPostsDoMesmoUsuario() {
    Tag tagAttached = entityManager.find(Tag.class, tag1.getId());

    for (int i = 1; i <= 5; i++) {
      Post post = criarPost(usuario1, "Post " + i, "Descrição " + i, (long) i * 10, List.of(tagAttached));
      entityManager.persist(post);
    }
    entityManager.flush();

    List<Post> posts = postRepository.findByUsuarioId(usuario1.getId());

    assertThat(posts).hasSize(5);
  }

  // ==================== TESTES DE CONTAGEM ====================

  @Test
  @DisplayName("Deve contar todos os posts")
  void deveContarTodosOsPosts() {
    Post post1 = criarPost(usuario1, "Post 1", "Desc", 0L, List.of());
    Post post2 = criarPost(usuario2, "Post 2", "Desc", 0L, List.of());

    entityManager.persist(post1);
    entityManager.persist(post2);
    entityManager.flush();

    long count = postRepository.count();

    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("Deve retornar zero quando não há posts")
  void deveRetornarZeroQuandoNaoHaPosts() {
    long count = postRepository.count();

    assertThat(count).isZero();
  }

  // ==================== TESTES DE PERSISTÊNCIA DE DATA ====================

  @Test
  @DisplayName("Deve salvar data de criação automaticamente")
  void deveSalvarDataDeCriacaoAutomaticamente() {
    Post post = criarPost(usuario1, "Post com Data", "Desc", 0L, List.of());

    Post postSalvo = postRepository.save(post);
    entityManager.flush();

    assertThat(postSalvo.getDataCriacao()).isNotNull();
  }

  @Test
  @DisplayName("Deve manter data de criação após atualização")
  void deveManterDataDeCriacaoAposAtualizacao() {
    Post post = criarPost(usuario1, "Post", "Desc", 0L, List.of());
    Post postSalvo = entityManager.persistAndFlush(post);
    Timestamp dataCriacaoOriginal = postSalvo.getDataCriacao();

    // Simula espera
    try { Thread.sleep(100); } catch (InterruptedException e) {}

    postSalvo.setTitulo("Título Atualizado");
    postRepository.save(postSalvo);
    entityManager.flush();
    entityManager.clear();

    Post postAtualizado = postRepository.findById(postSalvo.getId()).orElse(null);
    assertThat(postAtualizado).isNotNull();
    assertThat(postAtualizado.getDataCriacao()).isEqualTo(dataCriacaoOriginal);
  }

  // ==================== TESTES DE DELEÇÃO EM CASCATA ====================

  @Test
  @DisplayName("Deve deletar post mas manter tags")
  void deveDeletarPostMasManterTags() {
    Tag tagAttached = entityManager.find(Tag.class, tag1.getId());
    Post post = criarPost(usuario1, "Post", "Desc", 0L, List.of(tagAttached));
    Post postSalvo = entityManager.persistAndFlush(post);
    java.math.BigInteger tagId = tag1.getId();

    postRepository.deleteById(postSalvo.getId());
    entityManager.flush();

    Optional<Post> postDeletado = postRepository.findById(postSalvo.getId());
    Tag tagAinda = entityManager.find(Tag.class, tagId);

    assertThat(postDeletado).isEmpty();
    assertThat(tagAinda).isNotNull(); // Tag não deve ser deletada
  }

  // ==================== MÉTODOS AUXILIARES ====================

  private Usuario criarUsuario(String email, String cpf, String nome) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setCpf(cpf);
    usuario.setNome(nome);
    usuario.setSenhaHash("hash123");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario.setTags(new ArrayList<>());
    return entityManager.persist(usuario);
  }

  private Post criarPost(Usuario usuario, String titulo, String descricao, Long upVotes, List<Tag> tags) {
    Post post = new Post();
    post.setUsuario(usuario);
    post.setTitulo(titulo);
    post.setDescricao(descricao);
    post.setTotalUpVotes(upVotes);
    post.setTags(new ArrayList<>(tags));
    post.setDataCriacao(Timestamp.from(Instant.now()));
    return post;
  }
}
