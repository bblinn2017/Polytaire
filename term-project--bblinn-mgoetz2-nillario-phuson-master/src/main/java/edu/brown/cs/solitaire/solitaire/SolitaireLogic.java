package edu.brown.cs.solitaire.solitaire;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.brown.cs.solitaire.pile.Card;
import edu.brown.cs.solitaire.pile.Deck;
import edu.brown.cs.solitaire.pile.Move;

/**
 * This abstract class represents any kind of solitaire game.
 *
 * @author blinnbryce
 */
public abstract class SolitaireLogic {

  private SolitaireGame game;

  /**
   * This is the constructor for a solitaire logic class.
   *
   * @param g
   *          the game
   */
  public SolitaireLogic(SolitaireGame g) {
    game = g;
  }

  abstract public boolean isQueue(Move m);

  /**
   * This moves the cards on and including the input card to a destination
   * position. This assumes the move is valid.
   *
   * @param move
   *          the input move
   */
  public void implementStack(Move move) {
    Position src = move.getSrcCard().getPosition();

    Deck deck = game.getPileMap().get(src.getPileType())
        .getDeck(src.getDeckNum());
    List<Card> cards = deck.popCardsFrom(src.getCardNum());

    if (isQueue(move)) {
      Collections.reverse(cards);
    }

    game.getPileMap().get(move.getDestPile()).getDeck(move.getDestDeck())
        .stackAllCards(cards);

    flipDeckIfNeeded(deck);
    flipCardsIfNeeded(cards, move.getSrcPile(), move.getDestPile());
  }

  abstract protected void flipDeckIfNeeded(Deck deck);

  abstract protected void flipCardsIfNeeded(List<Card> cards, PileType srcType,
      PileType destType);

  /**
   * returns one of the best moves randomly selected
   * @param depth
   *          the depth of which to calculate the best sequence of moves
   * @param numMoves
   *          the number of best sequences from which to select
   * @param seed
   *          for the random selection
   * @return one of the best move sequences as a list
   */
  abstract public List<Move> bestMove(int depth, int numMoves, Integer seed);

  /**
   * This gets all the valid moves in a game.
   *
   * @param game
   *          the game
   * @return the list of valid moves
   */
  public abstract Set<Move> allValidMoves();

  /**
   * performs a breadth first search and evaluates these moves to find the best
   * sequences
   * @param depth
   *          depth of which to perform this search
   * @param numChoices
   *          the best sequence amongst the best numChoices sequences to be
   *          selected from
   * @param seed
   *          seed for the random choice
   * @return the list of moves sequence
   */
  public abstract List<Move> breadthSearcher(int depth, int numChoices,
      Integer seed);

  /**
   * This scores a move,
   *
   * @param move
   *          the move
   * @return the move's score
   */
  public abstract void scoreMove(Move move);

  /**
   * @return the game
   */
  public SolitaireGame getGame() {
    return game;
  }

  /**
   * @return Game over boolean value.
   */
  public abstract boolean gameIsOver();
}
