package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;



    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario createUsuario(UsuarioCreateDTO usuario) {
        Usuario newUsuario = usuario.toEntity();
        return usuarioRepository.save(newUsuario);
    }

    public Usuario updateUsuario(Integer id, UsuarioUpdateDTO usuarioDetails) {
      Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Erro na busca do usuário"));
      if (usuario != null){
        usuario = usuarioDetails.toUpdateEntity(usuario);
        return usuarioRepository.save(usuario);
      }
      return null;
    }

    public void deleteUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }

}
