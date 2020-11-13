package org.monospark.remix.sample;

import org.monospark.remix.Records;
import org.monospark.remix.Wrapped;
import org.monospark.remix.actions.Assign;
import org.monospark.remix.defaults.Default;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.monospark.remix.actions.Actions.*;
import static org.monospark.remix.defaults.Defaults.Now;


public class SortSample {

    public enum EventType {REGISTER, LOGIN, LOGOUT}

    public record Event(
            @Assign(NotNull.class)
            Wrapped<EventType> type,

            @Assign(NotNull.class)
            @Default(Now.class)
            Wrapped<Instant> timestamp)
            implements Comparable<Event> {
        @Override
        public int compareTo(Event o) {
            return timestamp.get().compareTo(o.timestamp.get());
        }
    }

    public record EventHistory(@Assign({Copy.class, Sort.class}) Wrapped<List<Event>> events) {
    }

    public static void checkOrdering() {
        var e1 = Records.create(Event.class, EventType.REGISTER, Instant.parse("2013-03-01T01:01:00Z"));
        var e2 = Records.create(Event.class, EventType.LOGOUT, Instant.parse("2013-03-01T07:05:00Z"));
        var e3 = Records.create(Event.class, EventType.LOGIN, Instant.parse("2013-03-01T02:15:00Z"));
        var e4 = Records.create(Event.class, EventType.LOGOUT);

        var history = Records.blank(EventHistory.class).set(EventHistory::events, new ArrayList<>())
                .add(EventHistory::events, e1, e2, e3, e4)
                .build();

        assert Records.get(history::events, 0).equals(e1);
        assert Records.get(history::events, 1).equals(e3);
        assert Records.get(history::events, 2).equals(e2);
        assert Records.get(history::events, 3).equals(e4);
    }
}
