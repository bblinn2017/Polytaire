package edu.brown.cs.solitaire.pile;

import java.util.List;

import edu.brown.cs.solitaire.solitaire.SolitaireGame;

public class MVDG {
	private Move move;
	private double value;
	private int depth;
	private SolitaireGame game;
	private List<Move> moveList;


public MVDG(Move move, double value, int depth, SolitaireGame game, List<Move> moveList) {
	this.move = move;
	this.value = value;
	this.depth = depth;
	this.game = game;
	this.moveList = moveList;
	
  }


/**
 * @return the moveList
 */
public List<Move> getMoveList() {
	return moveList;
}


/**
 * @return the game
 */
public SolitaireGame getGame() {
	return game;
}


/**
 * @return the move
 */
public Move getMove() {
	return move;
}


/**
 * @return the value
 */
public double getValue() {
	return value;
}


/**
 * @return the depth
 */
public int getDepth() {
	return depth;
}



}