package edu.brown.cs.solitaire.pile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import edu.brown.cs.solitaire.solitaire.PileType;

/**
 * This is any sort of ordered grouping of cards.
 * @author blinnbryce
 *
 */
public class Deck implements Iterable<Card> {

  private Deque<Card> deque;
  private PileType type;
  private int deckNum;

  /**
   * This is the constructor of a deck.
   * @param t
   *          type
   * @param d
   *          deck number
   */
  public Deck(PileType t, int d) {
    deque = new ConcurrentLinkedDeque<Card>();
    type = t;
    deckNum = d;
  }

  /**
   * This queues all input cards in their given order such that what is on the
   * top is the first card queued.
   * @param cards
   *          the input cards
   */
  public void queueAllCards(List<Card> cards) {
    // Add all to the deque
    deque.addAll(cards);
    Iterator<Card> iter = deque.descendingIterator();
    int i = 0;
    while (iter.hasNext()) {
      iter.next().setPosition(type, deckNum, i);
      i++;
    }
  }

  /**
   * This stacks all input cards in their given order such that what is on the
   * top is the first card stacked.
   * @param cards
   *          the input cards
   */
  public void stackAllCards(List<Card> cards) {
    for (Card c : cards) {
      c.setPosition(type, deckNum, deque.size());
      deque.push(c);
    }
  }

  /**
   * This adds a card to the top of the deck.
   * @param card
   *          input card
   */
  public void stackCard(Card card) {
    card.setPosition(type, deckNum, deque.size());
    deque.push(card);
  }

  /**
   * This pops all cards on top of and including the input card number and
   * returns a list such that the input card number is the first card.
   * @param cardNum
   *          the input card number
   * @return a list of cards
   */
  public List<Card> popCardsFrom(int cardNum) {
    List<Card> cards = new ArrayList<Card>();
    while (deque.size() > cardNum) {
      cards.add(0, deque.pop());
    }
    return cards;
  }

  /**
   * This retrieves but does not remove the top card on this deck.
   * @return the card on the top of this deck
   */
  public Card peekCard() {
    return deque.peek();
  }

  /**
   * This retrieves but does not remove the card at the input index in this
   * deck.
   * @param cardNum
   *          the input index
   * @return the card at the index
   */
  public Card peekCardAtIndex(int cardNum) {
    List<Card> cards = new ArrayList<Card>(deque);
    Collections.reverse(cards);
    if (cards.size() > cardNum) {
      return cards.get(cardNum);
    }
    return null;
  }

  /**
   * This returns all the current information about the deck.
   * @return the current deck information
   */
  public List<String[]> getDeckInfo() {
    List<String[]> deckStatus = new ArrayList<String[]>();
    synchronized (deque) {
      for (Card c : deque) {
        deckStatus.add(c.getCardInfo());
      }
    }
    return deckStatus;
  }

  /**
   * This returns the deque's length.
   * @return the deque length
   */
  public int getLength() {
    return deque.size();
  }

  @Override
  /**
   * This returns an iterator for the deck from top to bottom.
   * @return a deck iterator
   */
  public Iterator<Card> iterator() {
    return deque.iterator();
  }

  public int getDeckNum() {
    return deckNum;
  }

  public PileType getType() {
    return type;
  }

}
