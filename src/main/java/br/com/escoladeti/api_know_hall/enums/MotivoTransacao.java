package br.com.escoladeti.api_know_hall.enums;

public enum MotivoTransacao {
  UP_VOTE_COMENTARIO("Up vote em comentário"),
  SUPER_VOTE("Super vote em comentário"),
  UP_VOTE_POST("Up vote em post"),
  CONQUISTA("Conquista desbloqueada"),
  RESPOSTA_DESTAQUE("Resposta marcada como destaque"),
  GERADOR_QUALIDADE("Post gerou resposta destaque"),
  INSCRICAO_WORKSHOP_ALUNO("Inscrição em workshop como aluno"),
  INSCRICAO_WORKSHOP_INSTRUTOR("Pagamento recebido por workshop"),
  CANCELAMENTO_WORKSHOP_ALUNO("Cancelamento de inscrição em workshop como aluno"),
  CANCELAMENTO_WORKSHOP_INSTRUTOR("Reembolso pago por workshop"),
  PUNICAO("Punição por denúncia aceita"),
  STREAK_LOGIN("Bônus por login consecutivo");

  private final String descricao;

  MotivoTransacao(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
