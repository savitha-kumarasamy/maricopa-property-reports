package gov.maricopa.reports.schedule.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Frequencies {

    public static final Map<String, Duration> DELTAS = new LinkedHashMap<>();

    static {
        DELTAS.put("daily", Duration.ofDays(1));
        DELTAS.put("weekly", Duration.ofDays(7));
        DELTAS.put("monthly", Duration.ofDays(30));
        DELTAS.put("quarterly", Duration.ofDays(90));
    }

    private Frequencies() {
    }

    public static boolean isValid(String frequency) {
        return DELTAS.containsKey(frequency);
    }

    public static Duration delta(String frequency) {
        return DELTAS.getOrDefault(frequency, Duration.ofDays(30));
    }
}
