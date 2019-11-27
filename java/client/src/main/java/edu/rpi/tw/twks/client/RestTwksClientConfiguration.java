package edu.rpi.tw.twks.client;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.AbstractConfiguration;

import java.util.Properties;

public final class RestTwksClientConfiguration extends AbstractConfiguration {
    private final String serverBaseUrl;

    private RestTwksClientConfiguration(final Builder builder) {
        this.serverBaseUrl = builder.getServerBaseUrl();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("serverBaseUrl", serverBaseUrl);
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, RestTwksClientConfiguration> {
        private String serverBaseUrl = FieldDefinitions.SERVER_BASE_URL.getDefault();

        @Override
        public final RestTwksClientConfiguration build() {
            return new RestTwksClientConfiguration(this);
        }

        public final String getServerBaseUrl() {
            return serverBaseUrl;
        }

        public final Builder setServerBaseUrl(final String serverBaseUrl) {
            this.serverBaseUrl = serverBaseUrl;
            return this;
        }

        @Override
        public final Builder setFromProperties(final Properties properties) {
            return setServerBaseUrl(properties.getProperty(FieldDefinitions.SERVER_BASE_URL.getPropertyKey(), serverBaseUrl));
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinitionWithDefault<String> SERVER_BASE_URL = new ConfigurationFieldDefinitionWithDefault<>("http://localhost:8080", "twks.serverBaseUrl");
    }
}
