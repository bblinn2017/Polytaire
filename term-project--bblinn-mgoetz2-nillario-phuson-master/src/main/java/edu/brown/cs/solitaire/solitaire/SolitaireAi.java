package edu.brown.cs.solitaire.solitaire;

import java.util.List;

public abstract class SolitaireAi extends SolitaireGame {

  public SolitaireAi(SolitaireGame game) {
    super(game.getPileMap(), game.getStartTime());
  }

  @Override
  abstract public List<String[]> getBoardInfo();

  @Override
  abstract public SolitaireGame clone();

}
