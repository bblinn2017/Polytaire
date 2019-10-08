package edu.brown.cs.solitaire.solitare_action;

import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.klondike.KlondikePowHandler;
import edu.brown.cs.solitaire.klondike.KlondikePowHandler.KlondikePowerupType;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler.PowMessageType;

public class PowAction implements Action {

  @Override
  public JsonObject act(JsonObject received, SolitaireGame game) {
    SolitairePowHandler handler = new KlondikePowHandler();
    KlondikePowerupType type = KlondikePowerupType.values()[received
        .get("powType").getAsInt()];

    JsonObject payload = new JsonObject();
    payload.addProperty("powType", type.ordinal());
    PowMessageType messType = PowMessageType.values()[received
        .get("messageType").getAsInt()];

    if (messType == PowMessageType.INIT && !type.targetsSelf()) {
      // Send a warning message to players
      payload.addProperty("messageType", messType.ordinal());
      return payload;
    }

    payload.addProperty("messageType", PowMessageType.USE.ordinal());
    String str = handler.performPowerup(game, type);
    payload.addProperty("object", str);
    return payload;
  }

}
