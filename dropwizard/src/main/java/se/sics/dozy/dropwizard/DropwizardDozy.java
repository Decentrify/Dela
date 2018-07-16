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

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozySyncI;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class DropwizardDozy extends Application<DropwizardConfiguration> {

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
  public String getName() {
    return "Dropwizard";
  }

  @Override
  public void initialize(final Bootstrap<DropwizardConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/interface/", "/webapp/"));
  }

  @Override
  public void run(final DropwizardConfiguration configuration,
    final Environment environment) throws Exception {
    for (DozyResource resource : resources) {
      resource.initialize(syncInterfaces);
      environment.jersey().register(resource);
    }

    setupCors(environment);

    final int webPort = getServerPort(environment);
    LOG.info("{}running on port:{}", logPrefix, webPort);
//    setupFileLogs(configuration);
  }

  /*
   * To allow cross origin resource request from angular js client
   */
  private void setupCors(Environment environment) {

    final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

    // Configure CORS parameters
    cors.setInitParameter("allowedOrigins", "*");
    cors.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
    cors.setInitParameter("preflightMaxAge", "5184000"); // 2 months
    cors.setInitParameter("allowCredentials", "true");

    // Add URL mapping
    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
  }

  private int getServerPort(Environment environment) {
    return ((AbstractNetworkConnector) environment.getApplicationContext().getServer().getConnectors()[0])
      .getLocalPort();
  }

  @Override
  protected void bootstrapLogging() {
  }
//  private void setupFileLogs(Configuration configuration) {
//    LoggingConfiguration.FileConfiguration fileConfig
//      = configuration.getLoggingConfiguration().getFileConfiguration();
//    if (fileConfig == null) {
//      fileConfig = new LoggingConfiguration.FileConfiguration();
//      configuration.getLoggingConfiguration().setFileConfiguration(fileConfig);
//    }
//    if (!fileConfig.isConfigured()) {
//      fileConfig.setEnabled(true);
//      String logDir = delaBaseDir + File.separator + "logs" + File.separator;
//      fileConfig.setCurrentLogFilename(logDir + "dela.log");
//      fileConfig.setArchivedLogFilenamePattern(logDir + "dela-%d.log");
//      fileConfig.setArchivedFileCount(10);
//    }
//  }
}
