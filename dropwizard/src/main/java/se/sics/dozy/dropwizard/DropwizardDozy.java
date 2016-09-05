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
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozySyncI;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class DropwizardDozy extends Application<Configuration> {
    private static final Logger LOG = LoggerFactory.getLogger(DropwizardDozy.class);
    private String logPrefix = "";

    private final Map<String, DozySyncI> syncInterfaces;
    private final List<DozyResource> resources;
    
    public DropwizardDozy(Map<String, DozySyncI> syncInterfaces, List<DozyResource> resources) {
        this.syncInterfaces = syncInterfaces;
        this.resources = resources;
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/interface/", "/webapp/"));
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        for(DozyResource resource : resources) {
            resource.setSyncInterfaces(syncInterfaces);
            environment.jersey().register(resource);
        }
        
        /*
         * To allow cross origin resource request from angular js client
         */
//        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORSFilter", CrossOriginFilter.class);
//        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, environment.getApplicationContext().getContextPath() + "*");
//        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,OPTIONS");
//        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
//        filter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Origin, Content-Type, Accept");
//        filter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

//        environment.servlets().addFilter(CrossOriginFilter.class, "/*").
//                setInitParam("allowedOrigins", "*").
//                setInitParam("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin").
//                setInitParam("allowedMethods", "GET,PUT,POST,DELETE,OPTIONS").
//                setInitParam("preflightMaxAge", "5184000"). // 2 months
//                setInitParam("allowCredentials", "true");

//        final int webPort = configuration.getHttpConfiguration().getPort();
        int webPort = 0;
        SimpleServerFactory serverFactory = (SimpleServerFactory) configuration.getServerFactory();
        HttpConnectorFactory connector = (HttpConnectorFactory) serverFactory.getConnector();
        if (connector.getClass().isAssignableFrom(HttpConnectorFactory.class)) {
            webPort = connector.getPort();
        }
        LOG.info("{}running on port:{}", logPrefix, webPort);
    }
    
}

//extends Application<DropwizardDozyConfig> {
//
//    private static final Logger LOG = LoggerFactory.getLogger(DropwizardDozy.class);
//    private String logPrefix = "";
//
//    private final Map<String, DozySyncI> syncInterfaces;
//    private final List<DozyResource> resources;
//
//    public DropwizardDozy(Map<String, DozySyncI> syncInterfaces, List<DozyResource> resources) {
//        this.syncInterfaces = syncInterfaces;
//        this.resources = resources;
//    }
//
//    @Override
//    public void initialize(Bootstrap<DropwizardDozyConfig> bootstrap) {
//        bootstrap.addBundle(new ConfiguredAssetsBundle("/interface/", "/webapp/"));
//    }
//
//    @Override
//    public void run(DropwizardDozyConfig configuration, Environment environment) throws Exception {
//        for (DozyResource resource : resources) {
//            resource.setSyncInterfaces(syncInterfaces);
//            environment.jersey().register(resource);
//        }
//
//        /*
//         * To allow cross origin resource request from angular js client
//         */
//        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORSFilter", CrossOriginFilter.class);
//        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, environment.getApplicationContext().getContextPath() + "*");
//        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,OPTIONS");
//        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "allowed_host");
//        filter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Origin, Content-Type, Accept");
//        filter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
////        environment.servlets().addFilter(CrossOriginFilter.class, "/*").
////                setInitParam("allowedOrigins", "*").
////                setInitParam("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin").
////                setInitParam("allowedMethods", "GET,PUT,POST,DELETE,OPTIONS").
////                setInitParam("preflightMaxAge", "5184000"). // 2 months
////                setInitParam("allowCredentials", "true");
//
////        final int webPort = configuration.getHttpConfiguration().getPort();
//        int webPort = 0;
//        SimpleServerFactory serverFactory = (SimpleServerFactory) configuration.getServerFactory();
//        HttpConnectorFactory connector = (HttpConnectorFactory) serverFactory.getConnector();
//        if (connector.getClass().isAssignableFrom(HttpConnectorFactory.class)) {
//            webPort = connector.getPort();
//        }
//        LOG.info("{}running on port:{}", logPrefix, webPort);
//    }
//
//}
