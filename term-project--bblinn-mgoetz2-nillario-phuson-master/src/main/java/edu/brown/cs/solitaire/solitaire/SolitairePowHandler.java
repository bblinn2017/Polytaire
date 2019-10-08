package edu.brown.cs.solitaire.solitaire;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The handler for the powerups.
 * @author blinnbryce
 *
 */
public abstract class SolitairePowHandler {

  private Map<PowerupType, Function<SolitaireGame, String>> map;

  public SolitairePowHandler() {
    map = new HashMap<PowerupType, Function<SolitaireGame, String>>();
  }

  public void addToMap(PowerupType type, Powerup pow) {
    map.put(type, pow);
  }

  /**
   * Performs the action defined by the powerup on the group the player is
   * playing in.
   *
   * @param solitaregroup
   *          Group the player is playing in.
   * @param solitaregame
   *          The active player's game.
   * @param powerup
   *          Powerup to be acted upon.
   */
  public String performPowerup(SolitaireGame game, PowerupType type) {
    String s;
    synchronized (game) {
      s = map.get(type).apply(game);
    }
    return s;
  }

  public enum PowMessageType {
    ADD, USE, INIT
  }

  public interface PowerupType {
    public enum Target {
      OTHER, SELF
    }

    public Boolean targetsSelf();

    public int ord();

  }

  public abstract class Powerup implements Function<SolitaireGame, String> {

    private Function<SolitaireGame, String> function;

    public Powerup(Function<SolitaireGame, String> func) {
      function = func;
    }

    @Override
    public String apply(SolitaireGame game) {
      return function.apply(game);
    }
  }

}
