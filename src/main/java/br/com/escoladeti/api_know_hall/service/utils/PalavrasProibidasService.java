package br.com.escoladeti.api_know_hall.service.utils;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PalavrasProibidasService {

  // Palavras proibidas exatas (normalizadas)
  private static final Set<String> PALAVRAS_PROIBIDAS = Set.of(
    // Ofensas gerais
    "IDIOTA", "IMBECIL", "BURRO", "ESTUPIDO", "RETARDADO",

    // Palavrões comuns (moderados)
    "MERDA", "PORRA", "CARALHO", "BUCETA", "PUTA", "VIADO",
    "FDP", "PUTO", "CACETE", "ARROMBADO", "CU",

    // Discriminação e preconceito
    "NAZISMO", "NAZISTA", "HITLER", "RACISTA", "RACISMO",
    "HOMOFOBIA", "XENOFOBIA", "FASCISTA", "FASCISMO",

    // Conteúdo sexual/adulto
    "PORNO", "PORNOGRAFIA", "XXX", "SEXO", "PUTARIA",
    "XVIDEOS", "PORNHUB", "ONLYFANS",

    // Spam e fraudes
    "VIAGRA", "CIALIS", "CASINO", "CASSINO", "APOSTA",
    "BET", "GANHE DINHEIRO", "RENDA EXTRA", "CLIQUE AQUI",
    "COMPRE AGORA", "DESCONTO IMPERDIVEL",

    // Drogas
    "MACONHA", "COCAINA", "CRACK", "DROGA", "TRAFICANTE",
    "ERVA", "BECK", "BASEADO",

    // Violência
    "MATAR", "ASSASSINO", "TERRORISTA", "BOMBA", "ATENTADO",
    "SUICIDIO", "SUICIDA",

    // Abreviações e gírias ofensivas
    "FDP", "PQP", "VSF", "TMJ", "KRL", "PNC"
  );

  // Padrões regex para detectar variações e burlas
  private static final Set<Pattern> PADROES_PROIBIDOS = Set.of(
    // Variações de "idiota" (1d10t4, !d!0t@, etc)
    Pattern.compile(".*[i1!|][d][i1!|][o0][t][a4@].*", Pattern.CASE_INSENSITIVE),

    // Variações de "burro"
    Pattern.compile(".*[b][u][r]{2,}[o0].*", Pattern.CASE_INSENSITIVE),

    // Variações de "porno/porn" (p0rn, p@rn, p.o.r.n, etc)
    Pattern.compile(".*[p][.\\s_-]*[o0@][.\\s_-]*[r][.\\s_-]*[n].*", Pattern.CASE_INSENSITIVE),

    // Variações de "viagra" (v!agra, vi@gra, v.i.a.g.r.a)
    Pattern.compile(".*[v][.\\s_-]*[i1!|][.\\s_-]*[a4@][.\\s_-]*[g][.\\s_-]*[r][.\\s_-]*[a4@].*", Pattern.CASE_INSENSITIVE),

    // Variações de "casino/cassino"
    Pattern.compile(".*[c][a4@][s$][s$][i1!|][n][o0].*", Pattern.CASE_INSENSITIVE),

    // Variações de "sexo" (s3x0, s.e.x.o, sexx0)
    Pattern.compile(".*[s$][.\\s_-]*[e3][.\\s_-]*[x][.\\s_-]*[o0].*", Pattern.CASE_INSENSITIVE),

    // Variações de "puta" (put@, p.u.t.a, pût4)
    Pattern.compile(".*[p][.\\s_-]*[u][.\\s_-]*[t][.\\s_-]*[a4@].*", Pattern.CASE_INSENSITIVE),

    // Variações de "merda" (m3rd@, m.e.r.d.a)
    Pattern.compile(".*[m][.\\s_-]*[e3][.\\s_-]*[r][.\\s_-]*[d][.\\s_-]*[a4@].*", Pattern.CASE_INSENSITIVE),

    // Variações de "nazista" (n4z1st4, n@zist@)
    Pattern.compile(".*[n][a4@][z][i1!|][s$][t][a4@].*", Pattern.CASE_INSENSITIVE),

    // Variações de "hitler"
    Pattern.compile(".*[h][i1!|][t][l][e3][r].*", Pattern.CASE_INSENSITIVE),

    // Padrão para detectar repetição excessiva de caracteres (aaaaaaa, !!!!!!!!)
    Pattern.compile("(.)\\1{4,}"),

    // Padrão para detectar muitos números substituindo letras (h3ll0, t3st3)
    Pattern.compile(".*[a-z]*[0-9]{3,}[a-z]*.*", Pattern.CASE_INSENSITIVE),

    // Padrão para detectar URLs suspeitas
    Pattern.compile(".*(bit\\.ly|tinyurl|goo\\.gl|t\\.co).*", Pattern.CASE_INSENSITIVE),

    // Padrão para detectar múltiplos símbolos especiais (tentativa de bypass)
    Pattern.compile(".*[!@#$%^&*]{3,}.*"),

    // Variações de "FDP" (f.d.p, f_d_p, fdp)
    Pattern.compile(".*[f][.\\s_-]*[d][.\\s_-]*[p].*", Pattern.CASE_INSENSITIVE),

    // Variações de "drogas"
    Pattern.compile(".*[d][r][o0][g][a4@][s$].*", Pattern.CASE_INSENSITIVE),

    // Variações de "maconha" (m@c0nh@, mac0nha)
    Pattern.compile(".*[m][a4@][c][o0][n][h][a4@].*", Pattern.CASE_INSENSITIVE)
  );


  private String normalizar(String texto) {
    if (texto == null) return "";

    // Remove acentos
    String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
      .replaceAll("\\p{M}", "");

    return semAcentos.toUpperCase().trim();
  }

  private String removerCaracteresEspeciais(String texto) {
    return texto.replaceAll("[\\s._-]", "");
  }


  public boolean contemPalavraProibida(String texto) {
    if (texto == null || texto.isBlank()) {
      return false;
    }

    String textoNormalizado = normalizar(texto);
    String textoSemEspeciais = removerCaracteresEspeciais(textoNormalizado);

    if (PALAVRAS_PROIBIDAS.contains(textoNormalizado)) {
      return true;
    }

    for (String palavraProibida : PALAVRAS_PROIBIDAS) {
      if (textoNormalizado.contains(palavraProibida) ||
        textoSemEspeciais.contains(palavraProibida)) {
        return true;
      }
    }

    for (Pattern pattern : PADROES_PROIBIDOS) {
      if (pattern.matcher(texto).matches() ||
        pattern.matcher(textoNormalizado).matches() ||
        pattern.matcher(textoSemEspeciais).matches()) {
        return true;
      }
    }

    return false;
  }

  public String identificarPalavraProibida(String texto) {
    if (texto == null || texto.isBlank()) {
      return null;
    }

    String textoNormalizado = normalizar(texto);
    String textoSemEspeciais = removerCaracteresEspeciais(textoNormalizado);

    if (PALAVRAS_PROIBIDAS.contains(textoNormalizado)) {
      return textoNormalizado;
    }

    for (String palavraProibida : PALAVRAS_PROIBIDAS) {
      if (textoNormalizado.contains(palavraProibida) ||
        textoSemEspeciais.contains(palavraProibida)) {
        return palavraProibida;
      }
    }

    for (Pattern pattern : PADROES_PROIBIDOS) {
      if (pattern.matcher(textoNormalizado).matches() ||
        pattern.matcher(textoSemEspeciais).matches()) {
        return "padrão suspeito: " + pattern.pattern();
      }
    }

    return null;
  }
}
