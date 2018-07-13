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
package se.sics.dozy.dropwizard;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingConfiguration;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozySyncI;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class DropwizardDozy extends Service<Configuration> {

  private static final Logger LOG = LoggerFactory.getLogger(DropwizardDozy.class);
  private String logPrefix = "";

  private final Map<String, DozySyncI> syncInterfaces;
  private final List<DozyResource> resources;
  private final String delaBaseDir;

  public DropwizardDozy(Map<String, DozySyncI> syncInterfaces, List<DozyResource> resources, String delaBaseDir) {
    this.syncInterfaces = syncInterfaces;
    this.resources = resources;
    this.delaBaseDir = delaBaseDir;
  }

  @Override
  public void initialize(Bootstrap<Configuration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/interface/", "/webapp/"));
  }

  @Override
  public void run(Configuration configuration, Environment environment) throws Exception {
    for (DozyResource resource : resources) {
      resource.initialize(syncInterfaces);
      environment.addProvider(resource);
    }

    /*
     * To allow cross origin resource request from angular js client
     */
    environment.addFilter(CrossOriginFilter.class, "/*").
      setInitParam("allowedOrigins", "*").
      setInitParam("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin").
      setInitParam("allowedMethods", "GET,PUT,POST,DELETE,OPTIONS").
      setInitParam("preflightMaxAge", "5184000"). // 2 months
      setInitParam("allowCredentials", "true");

    final int webPort = configuration.getHttpConfiguration().getPort();
    LOG.info("{}running on port:{}", logPrefix, webPort);
    setupFileLogs(configuration);
  }

  private void setupFileLogs(Configuration configuration) {
    LoggingConfiguration.FileConfiguration fileConfig
      = configuration.getLoggingConfiguration().getFileConfiguration();
    if (fileConfig == null) {
      fileConfig = new LoggingConfiguration.FileConfiguration();
      configuration.getLoggingConfiguration().setFileConfiguration(fileConfig);
    }
    if (!fileConfig.isConfigured()) {
      fileConfig.setEnabled(true);
      String logDir = delaBaseDir + File.separator + "logs" + File.separator;
      fileConfig.setCurrentLogFilename(logDir + "dela.log");
      fileConfig.setArchivedLogFilenamePattern(logDir + "dela-%d.log");
      fileConfig.setArchivedFileCount(10);
    }
  }
}
