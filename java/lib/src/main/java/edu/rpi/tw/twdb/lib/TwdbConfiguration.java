package edu.rpi.tw.twdb.lib;

import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwdbConfiguration {
    private Optional<String> tdb2Location = Optional.empty();

    public final Optional<String> getTdb2Location() {
        return tdb2Location;
    }

    public final TwdbConfiguration setTdb2Location(final Optional<String> tdb2Location) {
        this.tdb2Location = checkNotNull(tdb2Location);
        return this;
    }

    public final TwdbConfiguration setFromSystemProperties() {
        return setFromProperties(System.getProperties());
    }

    public final TwdbConfiguration setFromProperties(final Properties properties) {
        final String tdb2Location = properties.getProperty(PropertyKeys.TDB2_LOCATION);
        if (tdb2Location != null) {
            this.tdb2Location = Optional.of(tdb2Location);
        }
        return this;
    }

    public final static class PropertyKeys {
        public final static String TDB2_LOCATION = "edu.rpi.tw.twdb.tdb2.location";
    }
}