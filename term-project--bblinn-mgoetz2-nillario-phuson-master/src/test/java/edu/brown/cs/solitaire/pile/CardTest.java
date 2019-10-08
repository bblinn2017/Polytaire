package edu.brown.cs.solitaire.pile;

import edu.brown.cs.solitaire.solitaire.Position;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CardTest {

  private Card aceofspades;
  private Card queenofspades;
  private Card tenofhearts;
  private Card nineofclubs;
  private Card queenofdiamonds;

  @Before
  public void before() {
    aceofspades = new Card(Card.Suit.S, Card.Symbol.ACE);
    tenofhearts = new Card(Card.Suit.H, Card.Symbol.TEN);
    nineofclubs = new Card(Card.Suit.C, Card.Symbol.NINE);
    queenofdiamonds = new Card(Card.Suit.D, Card.Symbol.QUEEN);
    queenofspades = new Card(Card.Suit.S, Card.Symbol.QUEEN);
  }

  @Test
  public void setFaceUp() {
    assertEquals(false, aceofspades.getFaceUp());
    aceofspades.setFaceUp(true);
    assertEquals(true, aceofspades.getFaceUp());
  }

  @Test
  public void getFaceUp() {
    tenofhearts.setFaceUp(true);
    assertEquals(true, tenofhearts.getFaceUp());
  }

  @Test
  public void getSymbol() {
    assertEquals(Card.Symbol.TEN, tenofhearts.getSymbol());
    assertEquals(Card.Symbol.ACE, aceofspades.getSymbol());
  }

  @Test
  public void getSuit() {
    assertEquals(Card.Suit.S, aceofspades.getSuit());
    assertEquals(Card.Suit.H, tenofhearts.getSuit());
  }

}