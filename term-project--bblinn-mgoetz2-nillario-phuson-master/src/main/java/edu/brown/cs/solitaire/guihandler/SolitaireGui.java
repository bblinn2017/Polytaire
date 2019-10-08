package edu.brown.cs.solitaire.guihandler;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * This runs the gui.
 *
 * @author marlene
 */
public class SolitaireGui {

  private static final String TITLE = "Polytaire";

  /**
   * This sets up the solitaire gui.
   *
   * @param freeMarker
   *          the free marker engine
   */
  public SolitaireGui(FreeMarkerEngine freeMarker) {
    // Setup Spark Routes
    Spark.port(getHerokuAssignedPort());
    Spark.webSocket("/update", SolitaireWebSocket.class);

    Spark.get("/", new HomeRedirectHandler());
    Spark.get("/solitaire", new SolitaireMainHandler(), freeMarker);
    Spark.get("/homepage", new SolitaireHomeHandler(), freeMarker);
    Spark.get("/hostGame", new SolitaireHostHandler(), freeMarker);
    Spark.get("/joinGame", new SolitaireJoinHandler(), freeMarker);
    Spark.get("/gameOver", new SolitaireGameOverHandler(), freeMarker);
    Spark.get("/play", new SolitairePlayHandler(), freeMarker);

  }

  /**
   * Handle main set up of our Solitaire webpage.
   *
   * @author blinnbryce
   */
  private static class SolitaireMainHandler implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request arg0, Response arg1) throws Exception {

      Map<String, Object> variables = ImmutableMap.of("title", TITLE);

      return new ModelAndView(variables, "main.ftl");
    }

  }

  /**
   * Handle setup of the home page of our Solitaire webpage.
   *
   * @author marlene
   */
  private static class SolitaireHomeHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      Map<String, Object> variables = ImmutableMap.of("title", TITLE);

      return new ModelAndView(variables, "homepage.ftl");
    }
  }

  /**
   * Handle setup of the host page of our Solitaire webpage.
   *
   * @author marlene
   */
  private static class SolitaireHostHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      Map<String, Object> variables = ImmutableMap.of("title", TITLE);

      return new ModelAndView(variables, "host_game.ftl");
    }
  }

  /**
   * Handle setup of the home page of our Solitaire webpage.
   *
   * @author marlene
   */
  private static class SolitaireJoinHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      Map<String, Object> variables = ImmutableMap.of("title", TITLE);

      return new ModelAndView(variables, "join_game.ftl");
    }
  }

  /**
   * Handle setup of the play page of our Solitaire webpage.
   *
   * @author blinnbryce
   */
  private static class SolitairePlayHandler implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request arg0, Response arg1) throws Exception {

      Map<String, Object> variables = ImmutableMap.of("title", TITLE);

      return new ModelAndView(variables, "solitaire.ftl");
    }
  }

  /**
   *
   * @author blinnbryce
   *
   */
  private static class SolitaireGameOverHandler implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request request, Response response)
        throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title", TITLE);

      return new ModelAndView(variables, "game_over.ftl");
    }
  }

  private static class HomeRedirectHandler implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {
      response.redirect("/solitaire");
      return null;
    }
  }

  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567; // return default port if heroku-port isn't set (i.e. on
                 // localhost)
  }

}
