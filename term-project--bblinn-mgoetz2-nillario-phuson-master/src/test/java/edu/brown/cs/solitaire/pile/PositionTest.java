package edu.brown.cs.solitaire.pile;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PositionTest {

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
  public void isSameColor() {
    assertEquals(true, tenofhearts.isSameColor(queenofdiamonds));
    assertEquals(true, aceofspades.isSameColor(queenofspades));
  }

  @Test
  public void isSameSuit() {
    assertEquals(true, queenofspades.isSameColor(aceofspades));
  }

  @Test
  public void getSymbolDifference() {
    assertEquals((long) 2,
        (long) queenofspades.getSymbolDifference(tenofhearts));
  }

  @Test
  public void fitsOn() {
    assertEquals(true, tenofhearts.fitsOn(nineofclubs, false));
  }
}