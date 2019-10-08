package edu.brown.cs.solitaire.klondike;

import edu.brown.cs.solitaire.pile.Card;
import edu.brown.cs.solitaire.pile.Move;
import edu.brown.cs.solitaire.solitaire.Position;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;
import edu.brown.cs.solitaire.solitaire.SolitaireLogic;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KlondikeLogicTest {


  private SolitaireGame game;
  private SolitaireLogic logic;
  private Move testmove;
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
    game = new KlondikeInitGame().cloneGame();
    logic = new KlondikeLogic(game);
    SolitairePowHandler pow = new KlondikePowHandler();
    pow.performPowerup(game, KlondikePowHandler.KlondikePowerupType.SOLVE);
    System.out.println(game);
    logic = new KlondikeLogic(game);
    nineofclubs.setPosition(Position.KlondikePileType.TABLEAU, 2, 1);
    testmove = new Move(nineofclubs, Position.KlondikePileType.FOUNDATIONS, 2);
  }

  @Test
  public void scoreTableauTest() {
  }

  @Test
  public void scoreStockTest() {
  }

  @Test
  public void scoreFoundationTest() {
  }

  @Test
  public void scoreMoveTest() {
    assertEquals(100, game.getScore());
    System.out.println(logic.getGame());
    logic.scoreMove(testmove);
    assertEquals(200, game.getScore());
  }
}