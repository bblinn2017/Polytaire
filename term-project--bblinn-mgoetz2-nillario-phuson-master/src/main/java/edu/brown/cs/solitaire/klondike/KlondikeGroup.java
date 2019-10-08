package edu.brown.cs.solitaire.klondike;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.brown.cs.solitaire.guihandler.MessageSender;
import edu.brown.cs.solitaire.guihandler.SolitaireWebSocket.ReqType;
import edu.brown.cs.solitaire.klondike.KlondikePowHandler.KlondikePowerupType;
import edu.brown.cs.solitaire.pile.Move;
import edu.brown.cs.solitaire.solitaire.SolitaireAi;
import edu.brown.cs.solitaire.solitaire.SolitaireGame;
import edu.brown.cs.solitaire.solitaire.SolitaireGroup;
import edu.brown.cs.solitaire.solitaire.SolitaireLogic;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler.PowerupType;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class KlondikeGroup extends SolitaireGroup {

  private static final Long powLow = (long) (4 * 1000);
  private static final Long powRange = (long) (5 * 1000);
  private static final Long aiLow = (long) (6 * 1000);
  private static final Long aiRange = (long) (4 * 1000);
  private static final int aiMoves = 4;
  private static final int aiChoices = 5;

  private static final int depth = 4;
  private static final Long aiFreezeTime = (long) (5 * 1000);
  private static boolean frozen = false;
  private static boolean gameOver;
  private static final int MAX_TIME = 300;

  public KlondikeGroup(String groupId, Map<Integer, Session> sessions,
      Set<Integer> aiIds) {
    super(groupId, sessions, new KlondikeInitGame(), aiIds);
    gameOver = false;
  }

  @Override
  public boolean gameOver() {
    return gameOver;
  }

  public static boolean gameFinished(SolitaireGame game) {
    SolitaireLogic logic = new KlondikeLogic(game);
    return logic.gameIsOver();
  }

  private static class KlondikePowerupThread extends PowerupThread {

    @Override
    public void run() {
      Map<Integer, SolitaireGame> games = getGames();
      for (Integer id : games.keySet()) {
        synchronized (games) {
          givePowerup(id, KlondikePowerupType.BLOCK);
        }
      }

      while (true) {
        Long millis = powLow + (long) (Math.random() * powRange);
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {
          return;
        }

        synchronized (games) {
          if (games.isEmpty()) {
            break;
          }

          // Give someone a powerup
          Integer min = Integer.MAX_VALUE;
          Integer id = null;
          for (Entry<Integer, SolitaireGame> e : games.entrySet()) {
            SolitaireGame game = e.getValue();
            if (game.getScore() < min) {
              min = game.getScore();
              id = e.getKey();
            }
          }

          // Give the loser and a random player a random powerup
          Random rand = new Random();
          PowerupType[] powerups = KlondikePowerupType.values();

          // Give powerup to loser
          givePowerup(id, powerups[rand.nextInt(powerups.length)]);

          // Give powerup to random
          Set<Integer> ids = games.keySet();
          Integer randId = new ArrayList<Integer>(ids)
              .get(rand.nextInt(ids.size()));
          givePowerup(randId, powerups[rand.nextInt(powerups.length)]);
        }
      }
    }
  }

  public static class KlondikeGameoverThread extends Thread {

    @Override
    public void run() {
      int start = getStartTime();
      while (true) {
        if ((int) (System.currentTimeMillis() / 1000) - start > MAX_TIME) {
          gameOver = true;
          break;
        }
        for (SolitaireGame game : getGames().values()) {
          if (gameFinished(game)) {
            gameOver = true;
            break;
          }
        }
        for (SolitaireAi aiGame : getAiGames().values()) {
          if (gameFinished(aiGame)) {
            gameOver = true;
            break;
          }
        }
      }

      Map<Integer, Integer> scores = new HashMap<Integer, Integer>();
      for (Entry<Integer, SolitaireGame> e : getGames().entrySet()) {
        SolitaireGame game = e.getValue();
        scores.put(e.getKey(), game.getScore());
      }
      for (Entry<Integer, SolitaireAi> e : getAiGames().entrySet()) {
        SolitaireGame game = e.getValue();
        scores.put(e.getKey(), game.getScore());
      }

      JsonObject payload = new JsonObject();
      Gson gson = new Gson();
      payload.addProperty("infoType", InfoType.GAME_OVER.ordinal());
      payload.addProperty("scores", gson.toJson(scores));

      JsonObject response = new JsonObject();
      response.addProperty("reqType", ReqType.SERVER.ordinal());
      response.addProperty("payload", payload.toString());
      response.addProperty("oppPayload", buildOpponentPayload().toString());

      // Game Over
      for (Session sess : getSessions().values()) {
        MessageSender sender = new MessageSender();
        sender.sendMessage(sess, response);
      }
      killThreads();
    }
  }

  private static class KlondikeAiThread extends AiThread {

    @Override
    public void run() {
      // RUN AI

      while (true) {
        Long millis = aiLow + (long) (Math.random() * aiRange);
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {
          return;
        }
        if (frozen) {
          try {
            Thread.sleep(aiFreezeTime);
          } catch (InterruptedException e) {
            return;
          }
          frozen = false;
        }

        // Make AI moves
        for (SolitaireAi game : getAiGames().values()) {
          synchronized (game) {
            SolitaireLogic logic = new KlondikeLogic(game);
            for(int i = 0; i < aiMoves; i++) {	           
            	Move move = logic.bestMove(depth, aiChoices, game.hashCode()).get(0);
              logic.implementStack(move);
              logic.scoreMove(move);
            }
          }
        }

        sendAiInfo();
      }
    }
  }

  @Override
  public void runThreads() {
    List<Thread> threads = getThreads();
    PowerupThread powThread = new KlondikePowerupThread();
    threads.add(powThread);
    powThread.start();

    if (!getAiGames().isEmpty()) {
      AiThread aiThread = new KlondikeAiThread();
      threads.add(aiThread);
      aiThread.start();
    }

    KlondikeGameoverThread gameOverThread = new KlondikeGameoverThread();
    threads.add(gameOverThread);
    gameOverThread.start();
  }

  @Override
  protected void handleAiPowerup(PowerupType type) {
    if (type == KlondikePowerupType.FREEZE) {
      frozen = true;
    }
  }
}
