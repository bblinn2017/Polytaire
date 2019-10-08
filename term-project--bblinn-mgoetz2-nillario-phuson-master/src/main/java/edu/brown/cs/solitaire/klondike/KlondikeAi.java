package edu.brown.cs.solitaire.klondike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.brown.cs.solitaire.pile.Pile;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;
import edu.brown.cs.solitaire.solitaire.SolitaireAi;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;

public class KlondikeAi extends SolitaireAi {

  public KlondikeAi(SolitaireGame game) {
    super(game);
  }

  @Override
  public List<String[]> getBoardInfo() {
    List<String[]> boardStatus = new ArrayList<String[]>();
    synchronized (getPileMap()) {
      for (Pile p : getPileMap().values()) {
        boardStatus.addAll(p.getPileInfo());
      }
    }
    return boardStatus;
  }

  @Override
  public SolitaireGame clone() {
    Map<KlondikePileType, Pile> newMap = new HashMap<KlondikePileType, Pile>();
    synchronized (getPileMap()) {
      for (Entry<KlondikePileType, Pile> e : getPileMap().entrySet()) {
        newMap.put(e.getKey(), e.getValue().clone());
      }
    }
    return new KlondikeGame(newMap, getStartTime());
  }

}
