package edu.brown.cs.solitaire.solitaire;

/**
 * This class represents a card's position on the board. The card number
 * represents its position relative to the bottom such that the bottom card is a
 * position 0.
 * @author blinnbryce
 *
 */
public class Position {

  /**
   * This represents which set of decks the card is in.
   * @author blinnbryce
   *
   */
  public enum KlondikePileType implements PileType {
    TABLEAU, STOCK, FOUNDATIONS;

    @Override
    public void setPileType(Position position) {
      position.type = this;
    }
  }

  private KlondikePileType type;
  private int deckNum;
  private int cardNum;

  /**
   * This is a constructor for a position.
   * @param t
   *          the type
   * @param d
   *          the deck number
   * @param c
   *          the card number
   */
  public Position(KlondikePileType t, int d, int c) {
    type = t;
    deckNum = d;
    cardNum = c;
  }

  /**
   * This gets the position's type.
   * @return the position's type
   */
  public KlondikePileType getPileType() {
    return type;
  }

  /**
   * This sets the deck number.
   * @param num
   *          the deck number
   */
  public void setDeckNum(int num) {
    deckNum = num;
  }

  /**
   * This gets the position's deck number.
   * @return the position's deck number
   */
  public int getDeckNum() {
    return deckNum;
  }

  /**
   * This sets the card number.
   * @param num
   *          the card number
   */
  public void setCardNum(int num) {
    cardNum = num;
  }

  /**
   * This gets the position's card number.
   * @return the position's card number
   */
  public int getCardNum() {
    return cardNum;
  }
}
