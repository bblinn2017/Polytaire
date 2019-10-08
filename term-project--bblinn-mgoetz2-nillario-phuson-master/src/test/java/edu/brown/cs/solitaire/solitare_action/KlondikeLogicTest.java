package edu.brown.cs.solitaire.solitare_action;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.brown.cs.solitaire.klondike.KlondikeInitGame;
import edu.brown.cs.solitaire.klondike.KlondikeLogic;
import edu.brown.cs.solitaire.klondike.KlondikePowHandler;
import edu.brown.cs.solitaire.klondike.KlondikePowHandler.KlondikePowerupType;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;
import edu.brown.cs.solitaire.solitaire.SolitaireLogic;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler;

public class KlondikeLogicTest {

  @Test
  public void testGameOver() {

    SolitaireGame game = new KlondikeInitGame().cloneGame();
    SolitairePowHandler pow = new KlondikePowHandler();
    for (int i = 0; i < 52; i++) {
      pow.performPowerup(game, KlondikePowerupType.SOLVE);
    }
    SolitaireLogic logic = new KlondikeLogic(game);
    assertTrue(logic.gameIsOver());
    assertTrue("GameOver Not Working", true);
  }

}
