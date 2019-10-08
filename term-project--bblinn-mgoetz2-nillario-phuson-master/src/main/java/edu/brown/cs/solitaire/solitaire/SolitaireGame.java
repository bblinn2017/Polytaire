package edu.brown.cs.solitaire.solitaire;

import edu.brown.cs.solitaire.pile.Pile;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;

import java.util.List;
import java.util.Map;

/**
 * This represents the board of a game of solitaire.
 *
 * @author blinnbryce
 */
public abstract class SolitaireGame implements Cloneable {

  public int defaultMultiplier = 1;
  public int tempMultiplier = 2;
  public int multiplyUntil;
  private Map<KlondikePileType, Pile> pileMap;
  private int score;
  private int startTime;

  /**
   * This is the constructor for a solitaire.
   */
  public SolitaireGame(Map<KlondikePileType, Pile> newMap, int start) {
    pileMap = newMap;
    startTime = start;
    multiplyUntil = startTime;
  }

  /**
   * This gets the pile map.
   *
   * @return the pile map
   */
  public Map<KlondikePileType, Pile> getPileMap() {
    return pileMap;
  }

  /**
   * This returns all of the current card information about the board.
   *
   * @return the current card information
   */
  public abstract List<String[]> getBoardInfo();

  /**
   * @return Current Score.
   */
  public int getScore() {
    return score;
  }

  /**
   * @param val The value by which to change the score. Must be positive or
   *            negative int.
   * @return updated score.
   */
  public int addToScore(int val) {
    score += val;
    return score;
  }

  public int getStartTime() {
    return startTime;
  }

  @Override
  abstract public SolitaireGame clone();

  /**
   * @param until The time multiplier will activate for.
   */
  public void setMultiplyUntil(int until) {
    multiplyUntil = until;
  }
}
