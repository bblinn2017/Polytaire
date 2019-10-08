package edu.brown.cs.solitaire.guihandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.solitaire.klondike.KlondikeGroup;
import edu.brown.cs.solitaire.solitaire.SolitaireGroup;

/**
 * This handles the creation of connections between clients and the server.
 *
 * @author blinnbryce
 */
@WebSocket
public class SolitaireWebSocket {
  private static final long TIMEOUT = 10 * 60 * 1000;
  // This is for when new groups are made
  private static Map<String, SolitaireGroup> groups = new HashMap<String, SolitaireGroup>();
  private static Map<String, Set<Integer>> groupHost = new HashMap<String, Set<Integer>>();
  private static Map<Integer, Session> sessions = new HashMap<Integer, Session>();
  private int nextId = 0;

  /**
   * This handles creating connections between the server and the client.
   *
   * @param session
   *          the session
   * @throws IOException
   */
  @OnWebSocketConnect
  public void connected(Session session) {
    // Add the session to the session map
    session.setIdleTimeout(TIMEOUT);
    sessions.put(nextId, session);

    // Build the response
    JsonObject response = new JsonObject();
    response.addProperty("reqType", ReqType.CONNECT.ordinal());
    response.addProperty("id", nextId);
    nextId++;

    // Send the message
    try {
      session.getRemote().sendString(response.toString());
    } catch (IOException e) {
      session.close();
    }
  }

  ;

  /**
   * This handles when a connection is closed between the server and the client.
   *
   * @param session
   *          the session
   * @param statusCode
   *          the status code of the session
   * @param reason
   *          the reason for the close
   */
  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {
    // Remove the session and the game
    Integer gameId = null;
    for (Entry<Integer, Session> e : sessions.entrySet()) {
      if (session.equals(e.getValue())) {
        gameId = e.getKey();
        sessions.remove(gameId);
        break;
      }
    }
    String groupId = null;
    for (Entry<String, Set<Integer>> e : groupHost.entrySet()) {
      Set<Integer> gameIds = e.getValue();
      if (gameIds.contains(gameId)) {
        groupId = e.getKey();
        gameIds.remove(gameId);
        if (gameIds.isEmpty()) {
          groupHost.remove(e.getKey());
        }
        break;
      }
    }
    if (groups.containsKey(groupId)) {
      groups.get(groupId).closeGame(gameId);
      if (groups.get(groupId).isEmpty()) {
        groups.remove(groupId).killThreads();
      }
    }
  }

  /**
   * This handles when a message is sent to the server from the client.
   *
   * @param session
   *          the session
   * @param message
   *          the message
   */
  @OnWebSocketMessage
  public void message(Session session, String message) {
    // Get the recieved json object
    Gson gson = new Gson();
    JsonObject received = gson.fromJson(message, JsonObject.class);
    Integer type = received.get("reqType").getAsInt();

    // Make sure the type exists in request types
    assert (type == ReqType.CLIENT.ordinal() || type == ReqType.LINK.ordinal()
        || type == ReqType.JOIN.ordinal());

    String groupId = received.get("groupId").getAsString();
    if (type == ReqType.LINK.ordinal()) {
      // Get the info sent by the host
      GameType gameType = GameType.values()[received.get("gameType")
          .getAsInt()];
      Map<Integer, Session> ids = new HashMap<Integer, Session>();
      for (Integer id : groupHost.get(groupId)) {
        ids.put(id, sessions.get(id));
      }

      // Create the new Solitaire Group
      Integer numAi = received.get("AI").getAsInt();
      Set<Integer> aiIds = new HashSet<Integer>();
      for (int i = 0; i < numAi; i++) {
        aiIds.add(nextId);
        nextId++;
      }
      if (gameType == GameType.KLONDIKE) {
        groups.put(groupId, new KlondikeGroup(groupId, ids, aiIds));
      }
      return;
    } else if (type == ReqType.JOIN.ordinal()) {
      // Join the client to a host
      PlayerType playType = PlayerType.values()[received.get("player")
          .getAsInt()];
      Integer id = received.get("id").getAsInt();
      if (playType == PlayerType.HOST) {
        // Create the group id list
        groupHost.put(groupId, new HashSet<Integer>());
      }

      JsonObject response = new JsonObject();
      response.addProperty("reqType", ReqType.JOIN.ordinal());
      response.addProperty("playerType", playType.ordinal());
      // Wrong id
      if (playType == PlayerType.JOIN && !groupHost.containsKey(groupId)) {
        response.addProperty("success", false);
        MessageSender sender = new MessageSender();
        sender.sendMessage(session, response);
        return;
      }

      Set<Integer> ids = groupHost.get(groupId);
      ids.add(id);
      response.addProperty("groupId", groupId);
      response.addProperty("num", ids.size());
      response.addProperty("success", true);

      for (Integer sessId : ids) {
        MessageSender sender = new MessageSender();
        sender.sendMessage(sessions.get(sessId), response);
      }
      return;
    }

    // The Req Type is client
    Integer id = received.get("id").getAsInt();
    JsonObject payload = received.get("payload").getAsJsonObject();

    // Update the group
    SolitaireGroup group = groups.get(groupId);
    List<JsonObject> payloads = group.updateGroup(payload, id);
    JsonObject myPayload = payloads.get(0);
    JsonObject oppPayload = payloads.get(1);

    for (Entry<Integer, Session> e : group.getSessions().entrySet()) {
      Session sess = e.getValue();

      // Send response
      JsonObject response = new JsonObject();
      response.addProperty("reqType", ReqType.SERVER.ordinal());
      response.addProperty("id", id);
      response.addProperty("oppPayload", oppPayload.toString());
      response.addProperty("payload", myPayload.toString());

      MessageSender sender = new MessageSender();
      sender.sendMessage(sess, response);
    }

    if (group.gameOver()) {
      groups.remove(groupId);
    }
  }

  /**
   * This prints the stack trace when the websocket fails.
   *
   * @param e
   *          the throwable
   */
  @OnWebSocketError
  public void onError(Throwable e) {
    if (e instanceof TimeoutException) {
      System.out.println("SERVER HAS TIMED OUT");
      // Kill groups
      for (SolitaireGroup g : groups.values()) {
        g.killThreads();
        System.exit(0);
      }
    }
    e.printStackTrace();
  }

  /**
   * This is the enum for the type of request being sent or received.
   *
   * @author blinnbryce
   */
  public enum ReqType {
    CONNECT, SERVER, CLIENT, LINK, JOIN
  }

  private enum PlayerType {
    HOST, JOIN
  }

  private enum GameType {
    KLONDIKE
  }

}
