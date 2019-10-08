package edu.brown.cs.solitaire.solitaire;

import java.util.List;
import java.util.Map;

import edu.brown.cs.solitaire.pile.Card;
import edu.brown.cs.solitaire.pile.Pile;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;

public abstract class SolitaireInitGame {

  private int startTime;
  private final Map<KlondikePileType, Pile> pileMap;

  protected SolitaireInitGame() {
    startTime = (int) (System.currentTimeMillis() / 1000);
    pileMap = createPiles();
    disperseDeck();
  }

  protected int getStartTime() {
    return startTime;
  }

  protected Map<KlondikePileType, Pile> getPileMap() {
    return pileMap;
  }

  /**
   * This populates the piles according to the game rules.
   */
  protected abstract void disperseDeck();

  /**
   * This creates the piles according to the game rules.
   */
  protected abstract Map<KlondikePileType, Pile> createPiles();

  abstract public SolitaireGame cloneGame();

  abstract public SolitaireAi cloneAi();

  protected abstract List<Card> newDeck();
}
