package se.sics.dozy.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;

public class DelaConfiguration extends Configuration {

  @JsonProperty("jerseyClient")
  private JerseyClientConfiguration clientConfig = new JerseyClientConfiguration();

  public DelaConfiguration() {
    super();
  }

  public int getServerPort() {
    HttpConnectorFactory connector = (HttpConnectorFactory) ((DefaultServerFactory) getServerFactory())
      .getApplicationConnectors().get(0);
    return connector.getPort();
  }

  public JerseyClientConfiguration getClientConfiguration() {
    return clientConfig;
  }
}
