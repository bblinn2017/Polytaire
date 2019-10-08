package edu.brown.cs.solitaire.pile;

import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;

import java.util.Arrays;

public class Move {
  private Card srcCard;
  private KlondikePileType destPile;
  private Integer destDeck;

  public Move(Card srcCard, KlondikePileType destPile, Integer destDeck) {
    this.srcCard = srcCard;
    this.destPile = destPile;
    this.destDeck = destDeck;
  }


  /**
   * @return the srcCard
   */
  public Card getSrcCard() {
    return srcCard;
  }

  /**
   * @param srcCard the srcCard to set
   */
  public void setSrcCard(Card srcCard) {
    this.srcCard = srcCard;
  }

  /**
   * @return the destPile
   */
  public KlondikePileType getDestPile() {
    return destPile;
  }

  /**
   * @param destPile the destPile to set
   */
  public void setDestPile(KlondikePileType destPile) {
    this.destPile = destPile;
  }

  /**
   * @return the destDeck
   */
  public Integer getDestDeck() {
    return destDeck;
  }

  /**
   * @param destDeck the destDeck to set
   */
  public void setDestDeck(Integer destDeck) {
    this.destDeck = destDeck;
  }

  /**
   * @return the srcPile
   */
  public KlondikePileType getSrcPile() {
    return srcCard.getPosition().getPileType();
  }

  @Override
  public final boolean equals(Object o) {
    if (!o.getClass().equals(this.getClass())) {
      return false;
    }

    Move m = (Move) o;

    if (!m.getDestDeck().equals(destDeck)) {
      return false;
    }
    if (m.getDestPile() != destPile) {
      return false;
    }

    return m.getSrcCard().equals(srcCard);
  }

  @Override
  public final int hashCode() {
    int[] hash = {
        srcCard.hashCode(), destPile.hashCode(), destDeck.hashCode()
    };

    return Arrays.hashCode(hash);
  }

  @Override
  public String toString() {
    return "Move [srcCard=" + srcCard.toString() + ", srcPile=" + getSrcPile()
        + ", destPile=" + destPile + ", destDeck=" + destDeck + "]";
  }

}
