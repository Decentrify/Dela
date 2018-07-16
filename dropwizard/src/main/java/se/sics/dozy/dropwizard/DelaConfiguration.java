package se.sics.dozy.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;

public class DelaConfiguration extends Configuration {

  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  public DelaConfiguration(int serverPort, int adminPort) {
    super();
    setServerPort(serverPort);
    setAdminPort(adminPort);
  }

  private void setServerPort(int serverPort) {
    // The following is to make sure it runs with a random port. parallel tests clash otherwise
    ((HttpConnectorFactory) ((DefaultServerFactory) getServerFactory()).getApplicationConnectors().get(0)).
      setPort(serverPort);
  }

  private void setAdminPort(int adminPort) {
    // this is for admin port
    ((HttpConnectorFactory) ((DefaultServerFactory) getServerFactory()).getAdminConnectors().get(0))
      .setPort(0);
  }
  
  @JsonProperty("jerseyClient")
  public JerseyClientConfiguration getJerseyClientConfiguration() {
    return jerseyClient;
  }
  
}
