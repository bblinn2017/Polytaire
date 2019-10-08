package edu.brown.cs.solitaire.solitaire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.guihandler.MessageSender;
import edu.brown.cs.solitaire.guihandler.SolitaireWebSocket.ReqType;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler.PowMessageType;
import edu.brown.cs.solitaire.solitaire.SolitairePowHandler.PowerupType;
import edu.brown.cs.solitaire.solitare_action.Action;
import edu.brown.cs.solitaire.solitare_action.AiAction;
import edu.brown.cs.solitaire.solitare_action.MoveAction;
import edu.brown.cs.solitaire.solitare_action.NewAction;
import edu.brown.cs.solitaire.solitare_action.PowAction;
import edu.brown.cs.solitaire.solitare_action.PowAiAction;

public abstract class SolitaireGroup {

  private final Map<InfoType, Action> actionMap = new HashMap<InfoType, Action>();
  private final Map<InfoType, AiAction> aiActionMap = new HashMap<InfoType, AiAction>();
  private String groupId;
  private static Map<Integer, Session> sessions;
  private static Map<Integer, SolitaireGame> games;
  private static Map<Integer, SolitaireAi> aiGames;
  private static SolitaireInitGame initGame;
  private static List<Thread> threads;

  public SolitaireGroup(String id, Map<Integer, Session> sess,
      SolitaireInitGame init, Set<Integer> aiIds) {
    groupId = id;
    sessions = sess;
    games = new HashMap<Integer, SolitaireGame>();
    aiGames = new HashMap<Integer, SolitaireAi>();
    initGame = init;
    threads = new ArrayList<Thread>();
    setUpActionMaps();
    makeGames(aiIds);
    runThreads();
  }

  abstract public void runThreads();

  /**
   * This sets up the action map.
   */
  private void setUpActionMaps() {
    actionMap.put(InfoType.NEW, new NewAction());
    actionMap.put(InfoType.MOVE, new MoveAction());
    actionMap.put(InfoType.POW, new PowAction());

    aiActionMap.put(InfoType.POW, new PowAiAction());
  }

  private void makeGames(Set<Integer> aiIds) {
    for (Integer id : sessions.keySet()) {
      games.put(id, initGame.cloneGame());
    }
    for (Integer aiId : aiIds) {
      aiGames.put(aiId, initGame.cloneAi());
    }
  }

  public List<Thread> getThreads() {
    return threads;
  }

  abstract public boolean gameOver();

  protected static int getStartTime() {
    return initGame.getStartTime();
  }

  protected static Map<Integer, SolitaireGame> getGames() {
    return games;
  }

  protected static Map<Integer, SolitaireAi> getAiGames() {
    return aiGames;
  }

  public static JsonObject buildOpponentPayload() {
    // Build the opponent payload
    Map<Integer, List<String[]>> oppBoards = new HashMap<Integer, List<String[]>>();
    Map<Integer, Integer> oppScores = new HashMap<Integer, Integer>();
    JsonObject oppPayload = new JsonObject();
    for (Entry<Integer, SolitaireGame> e : games.entrySet()) {
      Integer gId = e.getKey();
      SolitaireGame g = e.getValue();
      oppBoards.put(gId, g.getBoardInfo());
      oppScores.put(gId, g.getScore());
    }
    for (Entry<Integer, SolitaireAi> e : aiGames.entrySet()) {
      Integer gId = e.getKey();
      SolitaireGame g = e.getValue();
      oppBoards.put(gId, g.getBoardInfo());
      oppScores.put(gId, g.getScore());
    }

    Gson gson = new Gson();
    oppPayload.addProperty("boards", gson.toJson(oppBoards));
    oppPayload.addProperty("scores", gson.toJson(oppScores));
    oppPayload.addProperty("infoType", InfoType.OPP.ordinal());
    return oppPayload;
  }

  abstract protected void handleAiPowerup(PowerupType type);

  public List<JsonObject> updateGroup(JsonObject payload, Integer id) {
    InfoType infoType = InfoType.values()[payload.get("infoType").getAsInt()];
    SolitaireGame game = games.get(id);

    // Get new payload and add the info type
    JsonObject newPayload = actionMap.get(infoType).act(payload, game);
    if (aiActionMap.containsKey(infoType)) {
      for (SolitaireAi aiGame : aiGames.values()) {
        PowerupType type = aiActionMap.get(infoType).actOnAi(payload, aiGame);
        handleAiPowerup(type);
      }
    }
    newPayload.addProperty("infoType", infoType.ordinal());

    // Get opponent payload
    JsonObject oppPayload = buildOpponentPayload();

    // Build the payload list
    List<JsonObject> payloadList = new ArrayList<JsonObject>();
    payloadList.add(newPayload);
    payloadList.add(oppPayload);
    return payloadList;
  }

  public static Map<Integer, Session> getSessions() {
    return sessions;
  }

  public String getGroupId() {
    return groupId;
  }

  public void closeGame(Integer id) {
    synchronized (games) {
      games.remove(id);
      sessions.remove(id);
    }
  }

  public boolean isEmpty() {
    return sessions.isEmpty();
  }

  public static void killThreads() {
    try {
      for (Thread t : threads) {
        t.interrupt();
      }
    } catch (Exception e) {
      System.exit(1);
    }
  }

  public static void sendAiInfo() {
    Map<Integer, List<String[]>> oppBoards = new HashMap<Integer, List<String[]>>();
    Map<Integer, Integer> oppScores = new HashMap<Integer, Integer>();
    for (Entry<Integer, SolitaireGame> e : games.entrySet()) {
      Integer gId = e.getKey();
      SolitaireGame g = e.getValue();
      oppBoards.put(gId, g.getBoardInfo());
      oppScores.put(gId, g.getScore());
    }
    for (Entry<Integer, SolitaireAi> e : aiGames.entrySet()) {
      Integer gId = e.getKey();
      SolitaireGame g = e.getValue();
      oppBoards.put(gId, g.getBoardInfo());
      oppScores.put(gId, g.getScore());
    }

    JsonObject payload = new JsonObject();
    payload.addProperty("infoType", InfoType.AI.ordinal());

    JsonObject response = new JsonObject();
    response.addProperty("reqType", ReqType.SERVER.ordinal());
    response.addProperty("payload", payload.toString());
    response.addProperty("oppPayload", buildOpponentPayload().toString());

    for (Session sess : sessions.values()) {
      MessageSender sender = new MessageSender();
      sender.sendMessage(sess, response);
    }
  }

  public static void givePowerup(Integer id, PowerupType pow) {
    boolean contains = true;
    synchronized (games) {
      if (!games.containsKey(id)) {
        contains = false;
      }
    }
    if (!contains) {
      return;
    }
    JsonObject payload = new JsonObject();
    payload.addProperty("infoType", InfoType.POW.ordinal());
    payload.addProperty("messageType", PowMessageType.ADD.ordinal());
    payload.addProperty("powType", pow.ord());

    JsonObject response = new JsonObject();
    response.addProperty("reqType", ReqType.SERVER.ordinal());
    response.addProperty("payload", payload.toString());
    response.addProperty("oppPayload", buildOpponentPayload().toString());
    response.addProperty("id", id);

    Session sess = sessions.get(id);
    MessageSender sender = new MessageSender();
    sender.sendMessage(sess, response);
  }

  /**
   * This is the enum for the type of info the client asks for.
   *
   * @author blinnbryce
   */
  public enum InfoType {
    NEW, MOVE, OPP, POW, AI, GAME_OVER
  }

  protected static abstract class PowerupThread extends Thread {

    @Override
    abstract public void run();
  }

  protected static abstract class AiThread extends Thread {

    @Override
    abstract public void run();
  }
}
