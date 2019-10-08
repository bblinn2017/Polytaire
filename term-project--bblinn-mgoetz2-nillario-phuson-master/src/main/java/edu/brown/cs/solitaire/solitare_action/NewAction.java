package edu.brown.cs.solitaire.solitare_action;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.solitaire.SolitaireGame;

/**
 * This is an action for creating a new game.
 * @author blinnbryce
 *
 */
public class NewAction implements Action {

  @Override
  public JsonObject act(JsonObject oldPayload, SolitaireGame game) {
    // Return board info
    Gson gson = new Gson();
    JsonObject payload = new JsonObject();
    payload.addProperty("boardInfo", gson.toJson(game.getBoardInfo()));

    return payload;
  }

}
