package com.example.actors;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.example.backend.Settings;
import com.example.backend.SettingsImpl;
import com.example.models.backend.BoundingBox;
import com.example.models.backend.PointOfInterest;
import com.example.models.backend.PointOfInterest.UserPosition;
import com.example.models.backend.RegionId;
import com.example.models.backend.RegionPoints;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PositionSubscriber extends UntypedAbstractActor {

    public static Props props(ActorRef subscriber) {
        return Props.create(PositionSubscriber.class, () -> new PositionSubscriber(subscriber));
    }

    private final ActorRef subscriber;

    private final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private final SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());

    public PositionSubscriber(ActorRef subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * The current regions subscribed to
     */
    private Set<RegionId> regions = ImmutableSet.of();

    /**
     * The current bounding box subscribed to
     */
    private Optional<BoundingBox> currentArea = Optional.empty();

    /**
     * The unpublished position updates
     */
    private final Map<String, PointOfInterest> updates = new HashMap<>();

    private static Object TICK = new Object();

    private final Cancellable tickTask = getContext().system().scheduler().schedule(settings.SubscriberBatchInterval,
            settings.SubscriberBatchInterval, self(), TICK, getContext().dispatcher(), self());

    public void postStop() throws Exception {
        tickTask.cancel();
    }

    public void onReceive(Object msg) throws Exception {
        if (msg instanceof BoundingBox) {
            BoundingBox bbox = (BoundingBox) msg;

            // Calculate new regions
            Set<RegionId> newRegions = settings.GeoFunctions.regionsForBoundingBox(bbox);

            // Subscribe to any regions that we're not already subscribed to
            newRegions.stream().filter(r -> !regions.contains(r)).forEach(region ->
                    mediator.tell(new DistributedPubSubMediator.Subscribe(region.getName(), self()), self())
            );

            // Unsubscribe from any regions that we no longer should be subscribed to
            regions.stream().filter(r -> !newRegions.contains(r)).forEach(region ->
                    mediator.tell(new DistributedPubSubMediator.Unsubscribe(region.getName(), self()), self())
            );

            regions = newRegions;
            currentArea = Optional.of(bbox);

        } else if (msg instanceof UserPosition) {
            UserPosition pos = (UserPosition) msg;
            updates.put(pos.getId(), pos);

        } else if (msg instanceof RegionPoints) {
            RegionPoints points = (RegionPoints) msg;
            points.getPoints().stream().forEach(point ->
                    updates.put(point.getId(), point)
            );

        } else if (msg == TICK) {
            if (!updates.isEmpty()) {
                subscriber.tell(new PositionSubscriberProtocol.PositionSubscriberUpdate(currentArea, updates.values()), self());
                updates.clear();
            }
        }

    }

}
