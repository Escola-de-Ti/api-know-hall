package br.com.escoladeti.api_know_hall.projection.usuario;

public interface UsuarioDetalhesProjection {
  String getNome();

  String getBiografia();

  Integer getNivel();

  Long getXp();

  Long getTokens();

  Integer getQtdPosts();

  Integer getQtdComentarios();

  Integer getQtdUpVotes();

  Integer getQtdSuperVotes();

  Integer getQtdWorkshops();

  String getImagemUrl();
}
