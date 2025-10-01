package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

      @Autowired
      private UsuarioService usuarioService;

      @GetMapping()
      public ResponseEntity<List<Usuario>> getAllUsuarios() {
          List<Usuario> usuarios = usuarioService.getAllUsuarios();
          return ResponseEntity.ok(usuarios);
      }
}
