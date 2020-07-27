package com.example.actors;

import com.google.common.collect.ImmutableList;
import com.example.models.backend.BoundingBox;
import com.example.models.backend.PointOfInterest;

import java.util.Collection;
import java.util.Optional;

public abstract class PositionSubscriberProtocol {

    /**
     * An update of positions for a position subscriber.
     */
    public static class PositionSubscriberUpdate {
        private final Optional<BoundingBox> area;
        private final Collection<PointOfInterest> updates;

        public PositionSubscriberUpdate(Optional<BoundingBox> area, Collection<PointOfInterest> updates) {
            this.area = area;
            this.updates = ImmutableList.copyOf(updates);
        }

        public Optional<BoundingBox> getArea() {
            return area;
        }

        public Collection<PointOfInterest> getUpdates() {
            return updates;
        }
    }
}
