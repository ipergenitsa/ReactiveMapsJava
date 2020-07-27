package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.example.actors.ClientConnection;
import com.example.actors.RegionManagerClient;
import com.example.backend.BotManager;
import com.example.backend.RegionManager;
import com.example.backend.Settings;
import com.fasterxml.jackson.databind.JsonNode;
import play.Environment;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Application extends Controller {
  private final ActorSystem actorSystem;
  private Materializer materializer;
  private ActorRef regionManagerClient;

  @Inject
  public Application(Materializer materializer,
                     Environment environment,
                     ActorSystem actorSystem) {
    this.materializer = materializer;
    this.actorSystem = actorSystem;

    regionManagerClient = actorSystem.actorOf(RegionManagerClient.props(), "regionManagerClient");

    if (Cluster.get(actorSystem).getSelfRoles().stream().anyMatch(r -> r.startsWith("com/example/backend"))) {
      actorSystem.actorOf(RegionManager.props(), "regionManager");
    }

    if (Settings.SettingsProvider.get(actorSystem).BotsEnabled) {
      int id = 1;
      URL url = environment.resource("bots/" + id + ".json");
      List<URL> urls = new ArrayList<>();
      while (url != null) {
        urls.add(url);
        id++;
        url = environment.resource("bots/" + id + ".json");
      }
      actorSystem.actorOf(BotManager.props(regionManagerClient, urls));
    }
  }

  /**
   * The index page.
   */
  public Result index(Http.Request request) {
    return ok(views.html.index.render(request));
  }

  /**
   * The WebSocket
   */
  public WebSocket stream(String email) {
      return WebSocket.Json.accept(upstream -> createFlowForActor(email));
  }

  private Flow<JsonNode, JsonNode, ?> createFlowForActor(String email) {
    return ActorFlow.actorRef(out -> ClientConnection.props(email, out, regionManagerClient),
            actorSystem, materializer);
  }
}