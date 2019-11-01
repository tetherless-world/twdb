package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwksConfiguration extends AbstractConfiguration<TwksConfiguration> {
    private final static Path DUMP_DIRECTORY_PATH_DEFAULT = Paths.get("/dump");
    private final Path dumpDirectoryPath;

    protected TwksConfiguration(final Path dumpDirectoryPath) {
        this.dumpDirectoryPath = checkNotNull(dumpDirectoryPath);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Path getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("dumpDirectoryPath", dumpDirectoryPath);
    }

    public static class Builder extends AbstractConfiguration.Builder<Builder, TwksConfiguration> {
        private Path dumpDirectoryPath = DUMP_DIRECTORY_PATH_DEFAULT;

        @Override
        public TwksConfiguration build() {
            return new TwksConfiguration(dumpDirectoryPath);
        }

        public final Path getDumpDirectoryPath() {
            return dumpDirectoryPath;
        }

        public Builder setDumpDirectoryPath(final Path dumpDirectoryPath) {
            this.dumpDirectoryPath = dumpDirectoryPath;
            return this;
        }

        @Override
        public Builder setFromProperties(final Properties properties) {
            @Nullable final String dumpDirectoryPath = properties.getProperty(PropertyKeys.DUMP_DIRECTORY_PATH);
            if (dumpDirectoryPath != null) {
                this.dumpDirectoryPath = Paths.get(dumpDirectoryPath);
            }

            return this;
        }

        @Override
        public Builder setFromSystemProperties() {
            return setFromProperties(System.getProperties());
        }
    }

    public static class PropertyKeys {
        public final static String DUMP_DIRECTORY_PATH = "twks.dump";
    }
}
