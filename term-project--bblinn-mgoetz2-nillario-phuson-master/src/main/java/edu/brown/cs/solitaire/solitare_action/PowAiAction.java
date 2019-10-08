package edu.brown.cs.solitaire.solitare_action;

import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.klondike.KlondikePowHandler;
import edu.brown.cs.solitaire.klondike.KlondikePowHandler.KlondikePowerupType;
import edu.brown.cs.solitaire.solitaire.SolitaireAi;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler.PowerupType;

public class PowAiAction implements AiAction {

  @Override
  public PowerupType actOnAi(JsonObject received, SolitaireAi ai) {
    KlondikePowerupType type = KlondikePowerupType.values()[received
        .get("powType").getAsInt()];

    if (type.targetsSelf()) {
      return null;
    }

    SolitairePowHandler handler = new KlondikePowHandler();
    return type;
  }

}
