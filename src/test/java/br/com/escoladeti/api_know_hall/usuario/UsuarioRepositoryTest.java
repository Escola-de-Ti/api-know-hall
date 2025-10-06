package br.com.escoladeti.api_know_hall.usuario;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setCpf("12345678901");
        usuario.setNome("Test User");
        usuario.setSenhaHash("hashedPassword");
        usuario.setStatusUsuario(StatusUsuario.ATIVO);
        usuario.setTipoUsuario(TipoUsuario.ALUNO);
    }

    @Test
    void findAll_ShouldReturnAllUsuarios() {
        entityManager.persistAndFlush(usuario);

        List<Usuario> usuarios = usuarioRepository.findAll();

        assertNotNull(usuarios);
        assertTrue(usuarios.size() > 0);
        assertEquals("test@test.com", usuarios.get(0).getEmail());
    }

    @Test
    void findById_WithValidId_ShouldReturnUsuario() {
        Usuario savedUsuario = entityManager.persistAndFlush(usuario);

        Optional<Usuario> found = usuarioRepository.findById(savedUsuario.getId());

        assertTrue(found.isPresent());
        assertEquals("test@test.com", found.get().getEmail());
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmpty() {
        Optional<Usuario> found = usuarioRepository.findById(999);

        assertFalse(found.isPresent());
    }

    @Test
    void save_ShouldPersistUsuario() {
        Usuario savedUsuario = usuarioRepository.save(usuario);

        assertNotNull(savedUsuario.getId());
        assertEquals("test@test.com", savedUsuario.getEmail());

        Usuario foundUsuario = entityManager.find(Usuario.class, savedUsuario.getId());
        assertNotNull(foundUsuario);
        assertEquals("test@test.com", foundUsuario.getEmail());
    }

    @Test
    void deleteById_ShouldRemoveUsuario() {
        Usuario savedUsuario = entityManager.persistAndFlush(usuario);
        Integer id = savedUsuario.getId();

        usuarioRepository.deleteById(id);
        entityManager.flush();

        Usuario deletedUsuario = entityManager.find(Usuario.class, id);
        assertNull(deletedUsuario);
    }
}
