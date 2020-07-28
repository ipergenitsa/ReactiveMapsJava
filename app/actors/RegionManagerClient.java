package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.routing.FromConfig;
import backend.PointOfInterest;
import backend.RegionId;
import backend.RegionManagerProtocol;
import backend.Settings;
import backend.SettingsImpl;

/**
 * A client for the region manager, handles routing of position updates to the
 * regionManager on the right backend node.
 */
public class RegionManagerClient extends UntypedAbstractActor {
  public static Props props() {
      return Props.create(RegionManagerClient.class, RegionManagerClient::new);
  }

    private final ActorRef regionManagerRouter =
            getContext().actorOf(Props.empty().withRouter(FromConfig.getInstance()), "router");
    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());

    public void onReceive(Object msg) throws Exception {
        if (msg instanceof PointOfInterest.UserPosition) {
            PointOfInterest.UserPosition pos = (PointOfInterest.UserPosition) msg;
            RegionId regionId = settings.GeoFunctions.regionForPoint(pos.getPosition());
            regionManagerRouter.tell(new RegionManagerProtocol.UpdateUserPosition(regionId, pos), self());
        }
    }
}