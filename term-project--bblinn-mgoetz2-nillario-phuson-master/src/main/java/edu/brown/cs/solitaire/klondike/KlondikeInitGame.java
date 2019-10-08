package edu.brown.cs.solitaire.klondike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.brown.cs.solitaire.pile.Card;
import edu.brown.cs.solitaire.pile.Card.Suit;
import edu.brown.cs.solitaire.pile.Card.Symbol;
import edu.brown.cs.solitaire.pile.Pile;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;
import edu.brown.cs.solitaire.solitaire.SolitaireInitGame;

public class KlondikeInitGame extends SolitaireInitGame {

  private static final int TABLEAU_SIZE = 7;

  public KlondikeInitGame() {
    super();
  }

  @Override
  protected void disperseDeck() {
    List<Card> cards = newDeck();
    // Populate Tableau
    for (int i = 0; i < TABLEAU_SIZE; i++) {
      for (int j = 0; j < TABLEAU_SIZE - i; j++) {
        Card c = cards.remove(0);
        // Set face up card to be the last in the tableau stack
        if (TABLEAU_SIZE - (i + 1) == j) {
          c.setFaceUp(true);
        }
        getPileMap().get(KlondikePileType.TABLEAU).getDeck(j).stackCard(c);
      }
    }
    // Populate Stock
    getPileMap().get(KlondikePileType.STOCK).getDeck(0).queueAllCards(cards);
  }

  @Override
  protected Map<KlondikePileType, Pile> createPiles() {
    Map<KlondikePileType, Pile> map = new HashMap<KlondikePileType, Pile>();
    // Seven piles main table
    map.put(KlondikePileType.TABLEAU,
        new Pile(KlondikePileType.TABLEAU, TABLEAU_SIZE));
    // Four piles built up on
    map.put(KlondikePileType.FOUNDATIONS,
        new Pile(KlondikePileType.FOUNDATIONS, 4));
    // Additional cards for stock
    map.put(KlondikePileType.STOCK, new Pile(KlondikePileType.STOCK, 2));
    return map;
  }

  @Override
  public KlondikeGame cloneGame() {
    Map<KlondikePileType, Pile> newMap = new HashMap<KlondikePileType, Pile>();
    for (Entry<KlondikePileType, Pile> e : getPileMap().entrySet()) {
      newMap.put(e.getKey(), e.getValue().clone());
    }
    return new KlondikeGame(newMap, getStartTime());
  }

  /**
   * This creates a new 52-card shuffled list of cards.
   *
   * @return list of cards
   */
  @Override
  public List<Card> newDeck() {
    List<Card> deck = new ArrayList<Card>();
    for (Suit suit : Suit.values()) {
      for (Symbol symbol : Symbol.values()) {
        deck.add(new Card(suit, symbol));
      }
    }
    // Shuffle deck
    Collections.shuffle(deck);
    return deck;
  }

  @Override
  public KlondikeAi cloneAi() {
    return new KlondikeAi(cloneGame());
  }

}
