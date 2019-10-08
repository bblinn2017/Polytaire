package edu.brown.cs.solitaire.klondike;

import static edu.brown.cs.solitaire.solitaire.Position.KlondikePileType.FOUNDATIONS;
import static edu.brown.cs.solitaire.solitaire.Position.KlondikePileType.TABLEAU;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import edu.brown.cs.solitaire.pile.Card;
import edu.brown.cs.solitaire.pile.Card.Symbol;
import edu.brown.cs.solitaire.pile.Deck;
import edu.brown.cs.solitaire.pile.MVDG;
import edu.brown.cs.solitaire.pile.MVDGComparator;
import edu.brown.cs.solitaire.pile.Move;
import edu.brown.cs.solitaire.pile.Pile;
import edu.brown.cs.solitaire.solitaire.PileType;
import edu.brown.cs.solitaire.solitaire.Position;
import edu.brown.cs.solitaire.solitaire.Position.KlondikePileType;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;
import edu.brown.cs.solitaire.solitaire.SolitaireLogic;

/**
 * This represents the logic behind klondike solitaire.
 *
 * @author blinnbryce
 */
public class KlondikeLogic extends SolitaireLogic {

  private SolitaireGame game;
  private static final double DECAY = 1.20;
  // Scoring constants
  private static final int TABLEAU_SCORE = 0;
  private static final int FOUNDATION_SCORE = 10;
  private static final int FINAL_FOUNDATION_SCORE = 100;
  private static final int STOCK_SCORE = 1;

  /**
   * This is the constructor for a klondike game.
   *
   * @param g
   *          the input klondike game
   */
  public KlondikeLogic(SolitaireGame g) {
    super(g);
    game = g;
  }

  @Override
  public List<Move> bestMove(int depth, int numMoves, Integer seed) {
    return breadthSearcher(depth, numMoves, seed);

  }

  @Override
  public List<Move> breadthSearcher(int depth, int numChoices, Integer seed) {
    Queue<MVDG> nodes = new ArrayDeque<>();
    Set<Move> moveOpts = allValidMoves();
    List<MVDG> deepMoves = new ArrayList<>();
    for (Move move : moveOpts) {
      nodes.add(huerMove(move, 0, 0, game, new ArrayList<>()));
    }
    while (!nodes.isEmpty()) {
      MVDG curMvdg = nodes.remove();
      if (curMvdg.getDepth() == depth) {
        deepMoves.add(curMvdg);
      } else {
        SolitaireGame curGame = curMvdg.getGame();
        KlondikeLogic newGameLogic = new KlondikeLogic(curGame);
        for (Move nextMove : newGameLogic.allValidMoves()) {
          MVDG layerMove = newGameLogic.huerMove(nextMove, curMvdg.getValue(),
              curMvdg.getDepth() + 1, curGame, curMvdg.getMoveList());
          nodes.add(layerMove);
        }
      }

    }
    deepMoves.sort(new MVDGComparator());
    if (deepMoves.isEmpty()) {
      return new ArrayList<Move>();
    }

    int numPotential = Math.min(numChoices, deepMoves.size());
    Random rand = new Random(seed);
    int choice = rand.nextInt(numPotential);

    List<Move> optimalSequence = deepMoves.get(choice).getMoveList();
    return optimalSequence;
  }

  /**
   * evaluates a move with hueristics and adds its value to preval and the move
   * to the list of previous moves.
   * @param move
   *          the move being made
   * @param preval
   *          the value of all previous moves in this sequence
   * @param depth
   *          the depth of this move from the current game
   * @param game
   *          the solitaire game when this move is made
   * @param prevs
   *          the list of previous moves in this sequence
   * @return a MDVG that represents that can be passed in to progress the depth.
   */
  public MVDG huerMove(Move move, double preval, int depth, SolitaireGame game,
      List<Move> prevs) {
    int value = 0;
    Map<KlondikePileType, Pile> gameMap = game.getPileMap();
    Pile stock = gameMap.get(KlondikePileType.STOCK);
    Pile foundations = gameMap.get(FOUNDATIONS);
    Card srcCard = move.getSrcCard();
    Position sourcePos = srcCard.getPosition();
    Boolean flipStock = (stock.getDeck(1).peekCard() == null)
        && (stock.getDeck(0).peekCard() != null);
    int minFoundations = 13;
    int maxFoundations = 0;
    for (Deck d : foundations) {
      if (d.peekCard() == null) {
        minFoundations = 0;
        break;
      } else {
        minFoundations = Math.min(minFoundations,
            d.peekCard().getSymbol().ordinal());
        maxFoundations = Math.min(maxFoundations,
            d.peekCard().getSymbol().ordinal());
      }
    }

    int symbolNum = move.getSrcCard().getSymbol().ordinal();
    if (move.getDestPile() == KlondikePileType.FOUNDATIONS) {
      value += 20;
      if (symbolNum <= 2 + minFoundations) {
        value += 20;
      }
    }
    int cardNum = move.getSrcCard().getPosition().getCardNum();
    if (sourcePos.getPileType() == TABLEAU) {
      if (cardNum == 0) {
        value -= 30;
      } else {
        Deck srcDeck = game.getPileMap().get(TABLEAU)
            .getDeck(sourcePos.getDeckNum());
        Boolean newUncovered = !srcDeck.peekCardAtIndex(cardNum - 1)
            .getFaceUp();
        if (newUncovered) {
          value += 3 * cardNum + 30;
        }
      }

    }
    if (sourcePos.getPileType() == KlondikePileType.FOUNDATIONS) {

      value -= 40;
      if (symbolNum <= maxFoundations - 2) {
        value -= 40;
      }

      value -= 20;

    }
    if (sourcePos.getPileType() == KlondikePileType.STOCK) {
      value += 2;
    }
    if (move.getDestPile() == KlondikePileType.TABLEAU) {
      value += 1;
    }
    if (flipStock) {
      if (move.getDestPile() == KlondikePileType.STOCK) {
        value = 30;

      } else {
        value = 0;
      }
    }
    List<Move> newMoveList = new ArrayList<>(prevs);

    newMoveList.add(move);
    SolitaireLogic newLogic = new KlondikeLogic(game);
    newLogic.implementStack(move);

    return new MVDG(move, value + preval * DECAY, depth, newLogic.getGame(),
        newMoveList);
  }

  @Override
  public Set<Move> allValidMoves() {
    Set<Move> allMoves = new HashSet<Move>();
    Map<KlondikePileType, Pile> gameMap = game.getPileMap();
    Pile tableau = gameMap.get(TABLEAU);
    Pile foundations = gameMap.get(FOUNDATIONS);
    List<Card> topCards = new ArrayList<>();
    List<Card> foundCards = new ArrayList<>();

    for (Deck d : tableau) {
      if (d != null) {
        topCards.add(d.peekCard());
      }

    }
    for (Deck d : foundations) {
      if (d != null) {
        foundCards.add(d.peekCard());
      }
    }
    List<Card> allMovableCards = allMovableCards();
    List<Card> allSingleMovableCards = allSingleMovableCards();
    for (Card card : allMovableCards) {
      if (card.getSymbol() == Symbol.KING) {
        int i = 0;
        for (Card t : topCards) {
          if (t == null) {
            allMoves.add(new Move(card, TABLEAU, i));
          }
          i++;
        }
      } else {
        int i = 0;
        for (Card t : topCards) {

          if (t != null) {
            if (t.fitsOn(card, false)) {
              allMoves.add(new Move(card, TABLEAU, i));
            }
          }
          i++;
        }
      }
    }
    for (Card card : allSingleMovableCards) {

      for (int i = 0; i < 4; i++) {
        Deck d = foundations.getDeck(i);
        Card topCard = d.peekCard();
        if (topCard == null) {
          if ((card.getSymbol() == Symbol.ACE)
              && (card.getSuit().ordinal() == i)) {
            allMoves.add(new Move(card, FOUNDATIONS, i));
          }
        } else {

          if (card.fitsOn(topCard, true)) {
            allMoves.add(new Move(card, FOUNDATIONS, i));
          }
        }
      }
    }

    // Check stock moves
    Pile stock = gameMap.get(KlondikePileType.STOCK);
    // Check if there's card on stock
    Card stockCover = stock.getDeck(0).peekCard();
    if (stockCover == null) {
      // Check if there's card left over on face up part of stock
      Card stockTop = stock.getDeck(1).peekCard();
      if (stockTop != null) {
        allMoves.add(new Move(stock.getDeck(1).peekCardAtIndex(0),
            KlondikePileType.STOCK, 0));
      }
    } else {
      // There is card on stock, move it
      allMoves.add(new Move(stockCover, KlondikePileType.STOCK, 1));
    }
    return allMoves;
  }

  /**
   * determines all movable stacks of cards by the highest card
   * @return the list of cards.
   */
  public List<Card> allMovableCards() {
    Map<KlondikePileType, Pile> gameMap = game.getPileMap();

    List<Card> movableCards = new ArrayList<>();
    Pile tableau = gameMap.get(TABLEAU);
    Pile foundations = gameMap.get(FOUNDATIONS);
    Pile stock = gameMap.get(KlondikePileType.STOCK);
    Card stockTop = stock.getDeck(1).peekCard();
    if (stockTop != null) {
      movableCards.add(stockTop);
    }
    for (Deck d : foundations) {
      if (d.peekCard() != null) {
        movableCards.add(d.peekCard());
      }
    }
    for (Deck d : tableau) {
      if (d != null) {
        for (Card c : d) {

          if (c.getFaceUp()) {
            movableCards.add(c);

          } else {
            break;
          }
        }
      }
    }
    return movableCards;
  }

  /**
   * determines all single movable cards, i.e. those that can be placed in the
   * foundation.
   * @return the list of these cards
   */
  public List<Card> allSingleMovableCards() {
    Map<KlondikePileType, Pile> gameMap = game.getPileMap();

    List<Card> movableCards = new ArrayList<>();
    Pile tableau = gameMap.get(TABLEAU);
    Pile stock = gameMap.get(KlondikePileType.STOCK);
    Card stockTop = stock.getDeck(1).peekCard();
    if (stockTop != null) {
      movableCards.add(stockTop);
    }
    for (Deck d : tableau) {
      if (d != null) {
        Card c = d.peekCard();
        if (c != null) {
          movableCards.add(c);
        }
      }
    }
    return movableCards;
  }

  public int scoreTableau(KlondikePileType destType, Move move) {
    if (destType == KlondikePileType.TABLEAU) {
      return TABLEAU_SCORE;
    } else {
      if (move.getSrcCard().getSymbol() == Symbol.KING) {
        // Terminal State for finishing each foundation awards a lot of
        // points.
        return FINAL_FOUNDATION_SCORE;
      }
      return FOUNDATION_SCORE;
    }
  }

  public int scoreStock(KlondikePileType destType, Move move) {
    if (destType == KlondikePileType.TABLEAU) {
      if (move.getSrcPile() == KlondikePileType.FOUNDATIONS) {
        return -FOUNDATION_SCORE;
      }
      return STOCK_SCORE;
    } else if (destType == KlondikePileType.STOCK) {
      return 0;
    }
    return FOUNDATION_SCORE;
  }

  public int scoreFoundation(KlondikePileType destType, Move move) {
    if (destType == KlondikePileType.TABLEAU) {
      return -FOUNDATION_SCORE;
    }
    return FOUNDATION_SCORE;
  }

  @Override
  public void scoreMove(Move move) {
    int timeMultiplier = getTimeMultiplier();

    KlondikePileType srcType = move.getSrcCard().getPosition().getPileType();
    KlondikePileType destType = move.getDestPile();
    Integer final_score = 0;

    switch (srcType) {
    default:
      break;
    case TABLEAU: // Card was moved from the tableau
      final_score = scoreTableau(destType, move);
      break;
    case STOCK:
        final_score = scoreStock(destType, move);
        break;
    case FOUNDATIONS:
        final_score = scoreFoundation(destType, move);
        break;
    }
    if((System.currentTimeMillis() / 1000) < game.multiplyUntil) {
      game.addToScore(final_score * timeMultiplier * game.tempMultiplier);
    } else {
      game.addToScore(final_score * timeMultiplier * game.defaultMultiplier);
    }
  }

  /**
   * @return Game over boolean value.
   */
  @Override
  public boolean gameIsOver() {
    Map<KlondikePileType, Pile> gameMap = game.getPileMap();

    Pile foundations = gameMap.get(FOUNDATIONS);

    for (Deck d : foundations) {
      Card c = d.peekCard();
      if (c == null || c.getSymbol() != Symbol.KING) {
        return false;
      }
    }
    return true;
  }

  private int getTimeMultiplier() {
    int difference = (int) (System.currentTimeMillis() / 1000
        - game.getStartTime());
    // This defines a multiplier that decreases from 10 to 1 and after 300
    // seconds just stays at 1.
    int maxTime = 500;
    int numIntervals = 9;
    int interval = maxTime / numIntervals;
    if (difference < maxTime) {
      int multiplier = numIntervals - difference / interval + 1;
      return multiplier;
    }
    return 1;
  }

  @Override
  public boolean isQueue(Move m) {
    return m.getDestDeck() == 0 && m.getSrcPile() == KlondikePileType.STOCK;
  }

  @Override
  protected void flipDeckIfNeeded(Deck deck) {
    if (deck.getType() != KlondikePileType.STOCK) {
      // Set faceup status of top card to true
      if (deck.peekCard() != null) {
        deck.peekCard().setFaceUp(true);
      }
    }
  }

  @Override
  protected void flipCardsIfNeeded(List<Card> cards, PileType srcType,
      PileType destType) {
    if (srcType == KlondikePileType.STOCK
        && destType == KlondikePileType.STOCK) {
      for (Card c : cards) {
        c.setFaceUp(!c.getFaceUp());
      }
    }
  }

}
