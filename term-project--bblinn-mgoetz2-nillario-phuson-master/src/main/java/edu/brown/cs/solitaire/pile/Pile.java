package edu.brown.cs.solitaire.pile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.brown.cs.solitaire.pile.Card.Suit;
import edu.brown.cs.solitaire.pile.Card.Symbol;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;

/**
 * This represents a group of decks which have one type.
 * @author blinnbryce
 *
 */
public class Pile implements Iterable<Deck>, Cloneable {

  private KlondikePileType type;
  private Deck[] decks;
  private int length;

  /**
   * This is the constructor for a pile.
   * @param t
   *          the pile type
   * @param length
   *          the pile length
   */
  public Pile(KlondikePileType t, int l) {
    type = t;
    length = l;
    decks = new Deck[l];
    for (int i = 0; i < l; i++) {
      decks[i] = new Deck(type, i);
    }
  }

  /**
   * This gets the deck given by the input number.
   * @param deckNum
   *          the deck number
   * @return the deck
   */
  public Deck getDeck(int deckNum) {
    return decks[deckNum];
  }

  /**
   * This returns all the current information about the pile.
   * @return the current pile information
   */
  public List<String[]> getPileInfo() {
    List<String[]> pileStatus = new ArrayList<String[]>();
    synchronized (decks) {
      for (Deck d : decks) {
        pileStatus.addAll(d.getDeckInfo());
      }
    }
    return pileStatus;
  }

  /**
   * This gets the length of all of the decks in the pile.
   * @return the pile's deck lengths
   */
  public Map<Integer, Integer> getPileLengths() {
    Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    for (int i = 0; i < decks.length; i++) {
      map.put(i, decks[i].getLength());
    }
    return map;
  }

  @Override
  public Iterator<Deck> iterator() {
    List<Deck> deck = new ArrayList<Deck>(Arrays.asList(decks));
    return deck.iterator();
  }

  @Override
  public Pile clone() {
    Pile p = new Pile(type, length);
    synchronized (decks) {
      for (int i = 0; i < decks.length; i++) {
        List<Card> toStack = new ArrayList<Card>();
        Iterator<Card> cards = decks[i].iterator();
        while (cards.hasNext()) {
          toStack.add(0, cards.next().clone());
        }
        p.getDeck(i).stackAllCards(toStack);
      }
    }
    return p;
  }

  public Card findCard(Symbol sym, Suit suit) {
    synchronized (decks) {
      for (Deck d : decks) {
        Iterator<Card> iter = d.iterator();
        while (iter.hasNext()) {
          Card next = iter.next();
          if (next.getSuit() == suit && next.getSymbol() == sym) {
            return next;
          }
        }
      }
    }
    return null;
  }
}
