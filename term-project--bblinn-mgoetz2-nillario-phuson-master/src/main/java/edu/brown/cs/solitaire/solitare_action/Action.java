package edu.brown.cs.solitaire.solitare_action;

import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.solitaire.SolitaireGame;

/**
 * This is an interface for actions to be taken on the solitaire game as
 * inputted from the client.
 * @author blinnbryce
 *
 */
public interface Action {

  /**
   * This executes an action.
   * @param received
   *          the received information
   * @param solitaire
   *          the solitaire game
   * @param gson
   *          the gson object
   * @return the output information
   */
  JsonObject act(JsonObject received, SolitaireGame game);
}
