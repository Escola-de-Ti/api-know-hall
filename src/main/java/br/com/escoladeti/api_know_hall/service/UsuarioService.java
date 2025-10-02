package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Usuario createUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(Integer id, Usuario usuarioDetails) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            usuario.setNome(usuarioDetails.getNome() != null ? usuarioDetails.getNome() : usuario.getNome());
            usuario.setEmail(usuarioDetails.getEmail() != null ? usuarioDetails.getEmail() : usuario.getEmail());
            usuario.setSenhaHash(usuarioDetails.getSenhaHash() != null ? usuarioDetails.getSenhaHash() : usuario.getSenhaHash());
            usuario.setBiografia(usuarioDetails.getBiografia() != null ? usuarioDetails.getBiografia() : usuario.getBiografia());
            usuario.setTelefone(usuarioDetails.getTelefone() != null ? usuarioDetails.getTelefone() : usuario.getTelefone());
            usuario.setTelefone2(usuarioDetails.getTelefone2() != null ? usuarioDetails.getTelefone2() : usuario.getTelefone2());
            usuario.setIdImagemPerfil(usuarioDetails.getIdImagemPerfil() != null ? usuarioDetails.getIdImagemPerfil() : usuario.getIdImagemPerfil());
            usuario.setStatusUsuario(usuarioDetails.getStatusUsuario() != null ? usuarioDetails.getStatusUsuario() : usuario.getStatusUsuario());
            usuario.setTipoUsuario(usuarioDetails.getTipoUsuario() != null ? usuarioDetails.getTipoUsuario() : usuario.getTipoUsuario());
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    public void deleteUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }

}
