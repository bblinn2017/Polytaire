package edu.brown.cs.solitaire.solitare_action;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.klondike.KlondikeLogic;
import edu.brown.cs.solitaire.pile.Card;
import edu.brown.cs.solitaire.pile.Move;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;
import edu.brown.cs.solitaire.solitaire.SolitaireLogic;

/**
 * This is an action for moving a card to another location.
 * @author blinnbryce
 *
 */
public class MoveAction implements Action {

  @Override
  public JsonObject act(JsonObject received, SolitaireGame game) {
    Gson gson = new Gson();
    SolitaireLogic solitaire = new KlondikeLogic(game);

    // Get the game and the card info and the destination info
    Integer[] cardInfo = gson.fromJson(received.get("cardInfo"),
        Integer[].class);
    Integer[] destInfo = gson.fromJson(received.get("destInfo"),
        Integer[].class);

    // Attempt to move the card
    // cardInfo [pileType, deckNum, cardNum]
    // destInfo [pileType, deckNum]
    // Parse the card and destination info
    KlondikePileType cardPileType = KlondikePileType.values()[cardInfo[0]];
    Integer cardDeckNum = cardInfo[1];
    Integer cardCardNum = cardInfo[2];
    Card card = game.getPileMap().get(cardPileType).getDeck(cardDeckNum)
        .peekCardAtIndex(cardCardNum);
    KlondikePileType destPileType = KlondikePileType.values()[destInfo[0]];
    Integer destDeckNum = destInfo[1];

    // If the move is not valid, don't move
    Move move = new Move(card, destPileType, destDeckNum);

    boolean success = solitaire.allValidMoves().contains(move);
    if (success) {
      solitaire.scoreMove(move);
      solitaire.implementStack(move);
    }


    // Create payload
    JsonObject payload = new JsonObject();
    payload.addProperty("success", success);
    payload.addProperty("score", game.getScore());
    payload.addProperty("cardInfo", gson.toJson(cardInfo));
    Integer[] newDestInfo = {
        destInfo[0], destInfo[1]
    };
    payload.addProperty("destInfo", gson.toJson(newDestInfo));

    return payload;

  }

}
