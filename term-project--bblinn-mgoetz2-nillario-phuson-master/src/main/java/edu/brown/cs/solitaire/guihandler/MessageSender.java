package edu.brown.cs.solitaire.guihandler;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.JsonObject;

public class MessageSender {

  public MessageSender() {

  }

  public void sendMessage(Session sess, JsonObject response) {
    // Send payload
    synchronized (sess) {
      try {
        sess.getRemote().sendString(response.toString());
      } catch (IOException ex) {
        sess.close();
      }
    }
  }
}
