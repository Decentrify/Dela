/*
 * Copyright (C) 2016 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2016 Royal Institute of Technology (KTH)
 *
 * Dozy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.dozy.dropwizard.util;

import io.dropwizard.client.JerseyClientBuilder;
import java.util.Random;
import javax.ws.rs.client.Client;
import se.sics.ktoolbox.webclient.WebClient;
import se.sics.ktoolbox.webclient.WebClientBuilder;

public class DropwizardClientBuilder implements WebClientBuilder {

  private final JerseyClientBuilder builder;
  private final Random rand = new Random(1234);

  public DropwizardClientBuilder(JerseyClientBuilder builder) {
    this.builder = builder;
  }

  @Override
  public WebClient httpsInstance() {
    Client client = builder.build("DelaDropwizardClient_" + rand.nextLong());
    return new WebClient(client);
  }

  @Override
  public WebClient httpInstance() {
    Client client = builder.build("DelaDropwizardClient_" + rand.nextLong());
    return new WebClient(client);
  }
}
