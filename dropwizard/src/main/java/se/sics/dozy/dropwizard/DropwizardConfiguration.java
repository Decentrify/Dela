package se.sics.dozy.dropwizard;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;

public class DropwizardConfiguration extends Configuration {
  // TODO: implement service configuration

  public DropwizardConfiguration(int serverPort, int adminPort) {
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
}
