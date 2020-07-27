package com.example.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.routing.FromConfig;
import com.example.backend.RegionManagerProtocol.UpdateUserPosition;
import com.example.backend.Settings;
import com.example.backend.SettingsImpl;
import com.example.models.backend.PointOfInterest.UserPosition;
import com.example.models.backend.RegionId;

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
        if (msg instanceof UserPosition) {
            UserPosition pos = (UserPosition) msg;
            RegionId regionId = settings.GeoFunctions.regionForPoint(pos.getPosition());
            regionManagerRouter.tell(new UpdateUserPosition(regionId, pos), self());
        }
    }
}