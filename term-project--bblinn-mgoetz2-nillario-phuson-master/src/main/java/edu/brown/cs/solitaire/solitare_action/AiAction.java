package edu.brown.cs.solitaire.solitare_action;

import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.solitaire.SolitaireAi;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler.PowerupType;

public interface AiAction {

  PowerupType actOnAi(JsonObject received, SolitaireAi ai);
}
