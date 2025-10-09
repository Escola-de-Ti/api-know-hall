package br.com.escoladeti.api_know_hall.usuario;

import br.com.escoladeti.api_know_hall.controller.UsuarioController;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureWebMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;
    private UsuarioCreateDTO usuarioCreateDTO;
    private UsuarioUpdateDTO usuarioUpdateDTO;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail("test@test.com");
        usuario.setCpf("12345678901");
        usuario.setNome("Test User");
        usuario.setSenhaHash("hashedPassword");
        usuario.setStatusUsuario(StatusUsuario.ATIVO);
        usuario.setTipoUsuario(TipoUsuario.ALUNO);

        usuarioCreateDTO = new UsuarioCreateDTO();
        usuarioCreateDTO.setEmail("test@test.com");
        usuarioCreateDTO.setCpf("12345678901");
        usuarioCreateDTO.setNome("Test User");
        usuarioCreateDTO.setSenha("hashedPassword");
        usuarioCreateDTO.setTipoUsuario(TipoUsuario.ALUNO);

        usuarioUpdateDTO = new UsuarioUpdateDTO();
        usuarioUpdateDTO.setEmail("updated@test.com");
        usuarioUpdateDTO.setNome("Updated User");
    }

    @Test
    void getAllUsuarios_ShouldReturnListOfUsuarios() throws Exception {
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioService.getAllUsuarios()).thenReturn(usuarios);

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value("test@test.com"))
                .andExpect(jsonPath("$[0].nome").value("Test User"));

        verify(usuarioService, times(1)).getAllUsuarios();
    }

    @Test
    void getUsuarioById_WithValidId_ShouldReturnUsuario() throws Exception {
        when(usuarioService.getUsuarioById(1)).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nome").value("Test User"));

        verify(usuarioService, times(1)).getUsuarioById(1);
    }

    @Test
    void getUsuarioById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(usuarioService.getUsuarioById(999)).thenReturn(null);

        mockMvc.perform(get("/usuarios/999"))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).getUsuarioById(999);
    }

    @Test
    void createUsuario_WithValidData_ShouldReturnCreatedUsuario() throws Exception {
        when(usuarioService.createUsuario(any(UsuarioCreateDTO.class))).thenReturn(usuario);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nome").value("Test User"));

        verify(usuarioService, times(1)).createUsuario(any(UsuarioCreateDTO.class));
    }

    @Test
    void updateUsuario_WithValidData_ShouldReturnUpdatedUsuario() throws Exception {
        when(usuarioService.updateUsuario(anyInt(), any(UsuarioUpdateDTO.class))).thenReturn(usuario);

        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(usuarioService, times(1)).updateUsuario(anyInt(), any(UsuarioUpdateDTO.class));
    }

    @Test
    void updateUsuario_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(usuarioService.updateUsuario(anyInt(), any(UsuarioUpdateDTO.class))).thenReturn(null);

        mockMvc.perform(put("/usuarios/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioUpdateDTO)))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).updateUsuario(anyInt(), any(UsuarioUpdateDTO.class));
    }

    @Test
    void deleteUsuario_ShouldReturnNoContent() throws Exception {
        doNothing().when(usuarioService).deleteUsuario(1);

        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).deleteUsuario(1);
    }

    @Test
    void getAllUsuarios_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(usuarioService.getAllUsuarios()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isInternalServerError());

        verify(usuarioService, times(1)).getAllUsuarios();
    }
}
