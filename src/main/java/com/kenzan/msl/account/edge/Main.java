package com.kenzan.msl.account.edge;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.karyon.server.KaryonServer;
import io.swagger.api.AccountEdgeApi;
import io.swagger.api.impl.AccountEdgeApiOriginFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.DispatcherType;
import java.util.EnumSet;


public class Main {

    /**
     * Runs jetty server to expose jersey API and initializes Karyon server
     *
     * @param args String array
     * @throws Exception if server doesn't start
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("archaius.deployment.applicationId", "accountedge");
        Server jettyServer = new Server(9002);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(AccountEdgeApiOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES,
            AccountEdgeApi.class.getCanonicalName());
        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "io.swagger.jaxrs.json;io.swagger.jaxrs.listing;io.swagger.api");
        jerseyServlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");
        KaryonServer karyonServer = new KaryonServer();

        try {
            karyonServer.initialize();
            karyonServer.start();
            jettyServer.start();
            jettyServer.join();
        } finally {
            karyonServer.close();
            jettyServer.destroy();
        }
    }
}
