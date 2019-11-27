package edu.rpi.tw.twks.api;

import javax.annotation.Nullable;
import java.util.Properties;

public final class TwksGraphNameCacheConfiguration extends AbstractConfiguration {
    private final boolean enable;

    private TwksGraphNameCacheConfiguration(final boolean enable) {
        this.enable = enable;
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final boolean getEnable() {
        return enable;
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, TwksGraphNameCacheConfiguration> {
        private boolean enable = FieldDefinitions.ENABLE.getDefault();

        @Override
        public TwksGraphNameCacheConfiguration build() {
            return null;
        }

        public final boolean getEnable() {
            return enable;
        }

        public final Builder setEnable(final boolean enable) {
            this.enable = enable;
            return this;
        }

        @Override
        public Builder setFromProperties(final Properties properties) {
            {
                @Nullable final String enable = properties.getProperty(FieldDefinitions.ENABLE.getPropertyKey());
                if (enable != null) {
                    setEnable(true);
                }
            }

            return this;
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinitionWithDefault<Boolean> ENABLE = new ConfigurationFieldDefinitionWithDefault<>(Boolean.FALSE, "twks.enable");
    }
}
