package edu.brown.cs.solitaire.klondike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;

import edu.brown.cs.solitaire.pile.Card;
import edu.brown.cs.solitaire.pile.Card.Suit;
import edu.brown.cs.solitaire.pile.Card.Symbol;
import edu.brown.cs.solitaire.pile.Deck;
import edu.brown.cs.solitaire.pile.Move;
import edu.brown.cs.solitaire.pile.Pile;
import edu.brown.cs.solitaire.solitaire.Position;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;
import edu.brown.cs.solitaire.solitaire.SolitaireLogic;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler;

public class KlondikePowHandler extends SolitairePowHandler {

  public KlondikePowHandler() {
    super();
    this.addToMap(KlondikePowerupType.FREEZE, new Freeze());
    this.addToMap(KlondikePowerupType.BLOCK, new Block());
    this.addToMap(KlondikePowerupType.SHUFFLE, new Shuffle());
    this.addToMap(KlondikePowerupType.SOLVE, new Solve());
    this.addToMap(KlondikePowerupType.DOUBLESCORE, new DoubleScore());
  }

  public enum KlondikePowerupType implements PowerupType {

    FREEZE(Target.OTHER), SHUFFLE(Target.OTHER),
    SOLVE(Target.SELF), BLOCK(Target.SELF), DOUBLESCORE(Target.SELF);

    private Target tar;

    KlondikePowerupType(Target t) {
      tar = t;
    }

    @Override
    public Boolean targetsSelf() {
      return tar == Target.SELF;
    }

    @Override
    public int ord() {
      return this.ordinal();
    }
  }

  public static Deck getRandomDeck(SolitaireGame game) {
    Random r = new Random();
    Map<KlondikePileType, Pile> map = game.getPileMap();
    Pile p = map.get(KlondikePileType.TABLEAU);
    Deck d = p.getDeck(r.nextInt(p.getPileLengths().size()));
    return d;
  }

  private class Freeze extends Powerup {

    public Freeze() {
      super((game) -> {
        // Send random deck back
        Gson gson = new Gson();
        return gson.toJson(getRandomDeck(game).getDeckNum());
      });
    }

  }

  private class Block extends Powerup {

    public Block() {
      super((game) -> {
        return "BLOCKED";
      });
    }
  }

  private class Shuffle extends Powerup {

    public Shuffle() {
      super((game) -> {

        // Shuffle
        Deck d = getRandomDeck(game);

        // Pop all cards
        List<Card> deckCards = new ArrayList<Card>(d.popCardsFrom(0));

        // Shuffle cards
        Collections.shuffle(deckCards);

        // Stack cards back
        d.stackAllCards(deckCards);
        d.iterator().forEachRemaining(c -> {
          if (!c.equals(d.peekCard())) {
            c.setFaceUp(false);
          } else {
            c.setFaceUp(true);
          }
        });

        Gson gson = new Gson();
        Map<String, String> info = new HashMap<String, String>();
        info.put("deckNum", gson.toJson(d.getDeckNum()));
        info.put("newDeck", gson.toJson(d.getDeckInfo()));
        return gson.toJson(info);
      });
    }
  }

  private class Solve extends Powerup {

    public Solve() {
      super((game) -> {
        // Solve
        Map<KlondikePileType, Pile> map = game.getPileMap();
        Pile foundations = map.get(KlondikePileType.FOUNDATIONS);

        // Get random top card
        Random r = new Random();
        Suit[] values = Suit.values();
        Suit findSuit = values[r.nextInt(values.length)];
        Card card = foundations.getDeck(findSuit.ordinal()).peekCard();
        while (card != null && card.getSymbol() == Symbol.KING) {
          findSuit = values[r.nextInt(values.length)];
          card = foundations.getDeck(findSuit.ordinal()).peekCard();
        }

        Symbol findSym;
        if (card != null) {
          findSym = card.getSymbol().nextSymbol();
        } else {
          findSym = Symbol.ACE;
        }

        // Find next card
        // Check the stock
        KlondikePileType type = KlondikePileType.STOCK;
        Pile inPile = map.get(type);

        Card nextCard = inPile.findCard(findSym, findSuit);
        // If its not in the stock its in the tableau
        if (nextCard == null) {
          type = KlondikePileType.TABLEAU;
          inPile = map.get(type);
          nextCard = inPile.findCard(findSym, findSuit);
        }

        // Get the card's num and deck
        Position pos = nextCard.getPosition();
        Integer cardNum = pos.getCardNum();
        Deck inDeck = inPile.getDeck(pos.getDeckNum());

        // Pop the cards above including the found card
        List<Card> popped = inDeck.popCardsFrom(cardNum);

        // Remove the card on the top
        Card solveCard = popped.remove(0);

        // Put the cards back
        inDeck.stackAllCards(popped);

        // If type is tableau flip all cards beneath face down
        // If type is not tableau don't change any of their faces
        if (type == KlondikePileType.TABLEAU) {
          // If the card is face up then all cards below the top need to be
          // flipped
          // If the card is not face up then the cards to not need to be flipped
          if (solveCard.getFaceUp()) {
            inDeck.forEach(c -> {
              if (!c.equals(inDeck.peekCard())) {
                c.setFaceUp(false);
              } else {
                c.setFaceUp(true);
              }
            });
          }
        }

        // Put the solved card on the foundation
        solveCard.setFaceUp(true);
        foundations.getDeck(findSuit.ordinal()).stackCard(solveCard);

        Move m = new Move(solveCard, KlondikePileType.FOUNDATIONS,
            pos.getDeckNum());
        SolitaireLogic logic = new KlondikeLogic(game);
        logic.scoreMove(m);

        Gson gson = new Gson();
        Map<String, String> info = new HashMap<String, String>();
        info.put("pileType", gson.toJson(type.ordinal()));
        info.put("deckNum", gson.toJson(inDeck.getDeckNum()));
        info.put("newDeck", gson.toJson(inDeck.getDeckInfo()));
        info.put("movedCard", gson.toJson(solveCard.getCardInfo()));
        return gson.toJson(info);
      });
    }
  }

  private class DoubleScore extends Powerup {
    public DoubleScore(){
      super((game) -> {
        game.setMultiplyUntil((int) ((System.currentTimeMillis() / 1000) + 10));

        Gson gson = new Gson();
        Map<String, String> info = new HashMap<String, String>();
        info.put("result", "DOUBLED");
        return gson.toJson(info);
      });
    }
  }
}
