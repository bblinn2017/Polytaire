package edu.brown.cs.solitaire.pile;

import java.util.Arrays;

import edu.brown.cs.solitaire.solitaire.PileType;
import edu.brown.cs.solitaire.solitaire.Position;

/**
 * This is a card for Solitaire.
 * @author blinnbryce
 *
 */
public class Card implements Cloneable {

  /**
   * This represents the card's suit.
   * @author blinnbryce
   *
   */
  public enum Suit {
    D(Color.RED), H(Color.RED), C(Color.BLACK), S(Color.BLACK);

    /**
     * This represents the suit color.
     * @author blinnbryce
     *
     */
    private enum Color {
      RED, BLACK
    };

    private final Color color;

    Suit(Color c) {
      color = c;
    }

    /**
     * This returns the color of the suit.
     * @return the color
     */
    public Color getColor() {
      return color;
    }

    /**
     * Checks if the other card is the same color.
     * @param card
     *          the other card
     * @return if the cards are the same color
     */
    public boolean isSameColor(Card card) {
      return color.ordinal() == card.getSuit().getColor().ordinal();
    }
  }

  /**
   * This represents the card's symbol.
   * @author blinnbryce
   *
   */
  public enum Symbol {
    ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(
        8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13);

    private final int symbolValue;

    Symbol(Integer s) {
      symbolValue = s;
    }

    /**
     * This gets the symbol's integer value.
     * @return the symbol's integer value
     */
    public Integer getSymbolValue() {
      return symbolValue;
    }

    /**
     * This gets the difference between this symbol's integer value and the
     * input card's symbol integer value.
     * @param c
     *          the input card
     * @return the difference between the symbol values
     */
    public Integer getSymbolDifference(Card c) {
      return symbolValue - c.getSymbol().getSymbolValue();
    }

    public Symbol nextSymbol() {
      return Symbol.values()[this.ordinal() + 1];
    }
  }

  private Suit suit;
  private Symbol symbol;
  private Position position;
  private boolean faceUp;

  /**
   * This is the constructor for a card.
   * @param s
   *          the suit
   * @param val
   *          the symbol
   */
  public Card(Suit s, Symbol val) {
    suit = s;
    symbol = val;
    position = new Position(null, -1, -1);
    faceUp = false;
  }

  /**
   * This sets the face up status of the card to the input boolean.
   * @param b
   *          the input boolean
   */
  public void setFaceUp(boolean b) {
    faceUp = b;
  }

  /**
   * This returns the card's face up status.
   * @return the face up status
   */
  public boolean getFaceUp() {
    return faceUp;
  }

  /**
   * This gets the symbol of this card.
   * @return the symbol
   */
  public Symbol getSymbol() {
    return symbol;
  }

  /**
   * This gets the suit of this card.
   * @return the suit
   */
  public Suit getSuit() {
    return suit;
  }

  /**
   * This gets the position of this card.
   * @return the position
   */
  public Position getPosition() {
    return position;
  }

  /**
   * This sets the card's position.
   * @param type
   *          the pile type
   * @param d
   *          the deck number
   * @param c
   *          the card number
   */
  public void setPosition(PileType type, int d, int c) {
    type.setPileType(position);
    position.setDeckNum(d);
    position.setCardNum(c);
  }

  /**
   * This returns whether this card and the input card are the same color.
   * @param c
   *          the input card
   * @return whether they are they same color
   */
  public boolean isSameColor(Card c) {
    return suit.isSameColor(c);
  }

  /**
   * This returns whether this card and the input card are the same suit.
   * @param c
   *          the input card
   * @return whether they are the same suit
   */
  public boolean isSameSuit(Card c) {
    return suit == c.getSuit();
  }

  /**
   * This gets the integer difference between this card's symbol value and the
   * input card's symbol value.
   * @param c
   *          the input card
   * @return the difference between their symbol values
   */
  public Integer getSymbolDifference(Card c) {
    return symbol.getSymbolDifference(c);
  }

  /**
   * This returns all the current information about the card.
   * @return the current card information
   */
  public String[] getCardInfo() {
    String[] info = {
        suit.toString(), symbol.toString(),
        Integer.toString(position.getPileType().ordinal()),
        Integer.toString(position.getDeckNum()),
        Integer.toString(position.getCardNum()), Boolean.toString(faceUp)
    };
    return info;
  }

  @Override
  public final boolean equals(Object o) {
    if (!o.getClass().equals(this.getClass())) {
      return false;
    }

    Card c = (Card) o;

    if (c.getSuit() != suit) {
      return false;
    }
    return c.getSymbol() == symbol;
  }

  @Override
  public final int hashCode() {
    int[] hash = {
        suit.ordinal(), symbol.ordinal()
    };
    return Arrays.hashCode(hash);
  }

  @Override
  public Card clone() {
    Card newCard = new Card(suit, symbol);
    newCard.setFaceUp(faceUp);
    return newCard;
  }

  public Boolean fitsOn(Card nextCard, Boolean matchSuit) {
    Integer symDif = getSymbolDifference(nextCard);
    if (symDif != 1) {
      return false;
    }
    if (matchSuit) {
      if (this.suit == nextCard.getSuit()) {
        return true;
      } else {
        return false;
      }
    } else {
      if (!isSameColor(nextCard)) {
        return true;
      } else {
        return false;
      }
    }
  }
}
