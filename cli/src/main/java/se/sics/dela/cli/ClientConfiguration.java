package se.sics.dela.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.sics.ktoolbox.httpsclient.builder.JerseyClientConfiguration;

public class ClientConfiguration {

  @JsonProperty("jerseyClient")
  private JerseyClientConfiguration clientConfig = new JerseyClientConfiguration();

  public ClientConfiguration() {
    super();
  }

  public JerseyClientConfiguration getJerseyClientConfiguration() {
    return clientConfig;
  }
}
