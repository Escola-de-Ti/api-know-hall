package br.com.escoladeti.api_know_hall.util;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class LevelConfiguration {

  @Getter
  public static class LevelData {
    private final int nivel;
    private final long xpTotal;
    private final long diferencaProximoNivel;
    private final long tokensRecompensa;
    private final String badge;

    public LevelData(int nivel, long xpTotal, long diferencaProximoNivel, long tokensRecompensa, String badge) {
      this.nivel = nivel;
      this.xpTotal = xpTotal;
      this.diferencaProximoNivel = diferencaProximoNivel;
      this.tokensRecompensa = tokensRecompensa;
      this.badge = badge;
    }

    public boolean hasBadge() {
      return badge != null && !badge.isEmpty();
    }
  }

  public static final int MIN_LEVEL = 1;
  public static final int MAX_LEVEL = 30;

  private static final Map<Integer, LevelData> LEVELS = new HashMap<>();

  static {
    LEVELS.put(1, new LevelData(1, 0L, 100L, 0L, "Iniciante"));
    LEVELS.put(2, new LevelData(2, 100L, 264L, 100L, null));
    LEVELS.put(3, new LevelData(3, 364L, 466L, 150L, null));
    LEVELS.put(4, new LevelData(4, 830L, 702L, 200L, null));
    LEVELS.put(5, new LevelData(5, 1532L, 952L, 250L, "Explorador(a)"));
    LEVELS.put(6, new LevelData(6, 2484L, 1219L, 300L, null));
    LEVELS.put(7, new LevelData(7, 3703L, 1503L, 350L, null));
    LEVELS.put(8, new LevelData(8, 5206L, 1802L, 400L, null));
    LEVELS.put(9, new LevelData(9, 7008L, 2117L, 450L, null));
    LEVELS.put(10, new LevelData(10, 9125L, 2512L, 500L, "Veterano(a)"));
    LEVELS.put(11, new LevelData(11, 11637L, 2800L, 550L, null));
    LEVELS.put(12, new LevelData(12, 14437L, 3300L, 600L, null));
    LEVELS.put(13, new LevelData(13, 17737L, 3800L, 650L, null));
    LEVELS.put(14, new LevelData(14, 21537L, 4300L, 700L, null));
    LEVELS.put(15, new LevelData(15, 25837L, 4800L, 750L, "Especialista"));
    LEVELS.put(16, new LevelData(16, 30637L, 5300L, 800L, null));
    LEVELS.put(17, new LevelData(17, 35937L, 5800L, 850L, null));
    LEVELS.put(18, new LevelData(18, 41737L, 6300L, 900L, null));
    LEVELS.put(19, new LevelData(19, 48037L, 6800L, 950L, null));
    LEVELS.put(20, new LevelData(20, 54837L, 7300L, 1000L, "Mestre"));
    LEVELS.put(21, new LevelData(21, 62137L, 8000L, 1050L, null));
    LEVELS.put(22, new LevelData(22, 70137L, 8960L, 1100L, null));
    LEVELS.put(23, new LevelData(23, 79097L, 10035L, 1150L, null));
    LEVELS.put(24, new LevelData(24, 89132L, 11239L, 1200L, null));
    LEVELS.put(25, new LevelData(25, 100371L, 12588L, 1250L, "Lenda"));
    LEVELS.put(26, new LevelData(26, 112959L, 14098L, 1300L, null));
    LEVELS.put(27, new LevelData(27, 127057L, 15790L, 1350L, null));
    LEVELS.put(28, new LevelData(28, 142847L, 17685L, 1400L, null));
    LEVELS.put(29, new LevelData(29, 160532L, 19807L, 1450L, null));
    LEVELS.put(30, new LevelData(30, 180339L, 0L, 1500L, "Sábio(a)"));
  }

  public static int calculateLevel(long xp) {
    if (xp < 0) {
      return MIN_LEVEL;
    }

    for (int nivel = MAX_LEVEL; nivel >= MIN_LEVEL; nivel--) {
      LevelData data = LEVELS.get(nivel);
      if (xp >= data.getXpTotal()) {
        return nivel;
      }
    }

    return MIN_LEVEL;
  }

  public static LevelData getLevelData(int nivel) {
    if (nivel < MIN_LEVEL) {
      return LEVELS.get(MIN_LEVEL);
    }
    if (nivel > MAX_LEVEL) {
      return LEVELS.get(MAX_LEVEL);
    }
    return LEVELS.get(nivel);
  }

  public static long getXpForLevel(int nivel) {
    return getLevelData(nivel).getXpTotal();
  }

  public static long getXpToNextLevel(long currentXp) {
    int currentLevel = calculateLevel(currentXp);

    if (currentLevel >= MAX_LEVEL) {
      return 0L;
    }

    LevelData nextLevelData = LEVELS.get(currentLevel + 1);
    return nextLevelData.getXpTotal() - currentXp;
  }

  public static long getLevelReward(int nivel) {
    return getLevelData(nivel).getTokensRecompensa();
  }

  public static String getLevelBadge(int nivel) {
    return getLevelData(nivel).getBadge();
  }

  public static boolean hasLevelChanged(long oldXp, long newXp) {
    return calculateLevel(oldXp) != calculateLevel(newXp);
  }

  public static int[] getLevelsGained(long oldXp, long newXp) {
    int oldLevel = calculateLevel(oldXp);
    int newLevel = calculateLevel(newXp);

    if (newLevel <= oldLevel) {
      return new int[0];
    }

    int[] levels = new int[newLevel - oldLevel];
    for (int i = 0; i < levels.length; i++) {
      levels[i] = oldLevel + i + 1;
    }

    return levels;
  }

  public static double getLevelProgress(long currentXp) {
    int currentLevel = calculateLevel(currentXp);

    if (currentLevel >= MAX_LEVEL) {
      return 100.0;
    }

    LevelData currentLevelData = getLevelData(currentLevel);
    LevelData nextLevelData = getLevelData(currentLevel + 1);

    long xpInCurrentLevel = currentXp - currentLevelData.getXpTotal();
    long xpNeededForNextLevel = nextLevelData.getXpTotal() - currentLevelData.getXpTotal();

    return (xpInCurrentLevel * 100.0) / xpNeededForNextLevel;
  }
}
