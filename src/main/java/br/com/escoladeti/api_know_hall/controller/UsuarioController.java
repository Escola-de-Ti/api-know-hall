package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioCreateDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioLoginDTO;
import br.com.escoladeti.api_know_hall.dto.UsuarioUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

  @Autowired
  private UsuarioService usuarioService;

  @GetMapping
  public ResponseEntity<List<Usuario>> getAllUsuarios() {
    try {
      List<Usuario> usuarios = usuarioService.getAllUsuarios();
      return ResponseEntity.ok(usuarios);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Usuario> getUsuarioById(@PathVariable BigInteger id) {
    try {
      Usuario usuario = usuarioService.getUsuarioById(id);
      return ResponseEntity.ok(usuario);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping
  public ResponseEntity<Usuario> createUsuario(@RequestBody UsuarioCreateDTO usuario) {
    try {
      Usuario createdUsuario = usuarioService.createUsuario(usuario);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdUsuario);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Usuario> updateUsuario(@PathVariable BigInteger id, @RequestBody UsuarioUpdateDTO usuarioDetails) {
    try {
      Usuario updatedUsuario = usuarioService.updateUsuario(id, usuarioDetails);
      return ResponseEntity.ok(updatedUsuario);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUsuario(@PathVariable BigInteger id) {
    try {
      usuarioService.deleteUsuario(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody UsuarioLoginDTO loginDTO) {
    try {
      JwtTokenDTO token = usuarioService.login(loginDTO.email(), loginDTO.senha());
      if (token != null) {
        return ResponseEntity.ok(token);
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}

