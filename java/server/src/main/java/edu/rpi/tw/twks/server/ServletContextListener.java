package edu.rpi.tw.twks.server;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import edu.rpi.tw.twks.ext.ClasspathExtensions;
import edu.rpi.tw.twks.ext.FileSystemExtensions;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.servlet.JerseyServlet;
import org.apache.commons.configuration2.web.ServletContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;
import java.nio.file.Path;
import java.util.Optional;

public final class ServletContextListener implements javax.servlet.ServletContextListener {
    private final static Logger logger = LoggerFactory.getLogger(ServletContextListener.class);
    private ClasspathExtensions classpathExtensions;
    private FileSystemExtensions fileSystemExtensions;


    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        classpathExtensions.destroy();
        fileSystemExtensions.destroy();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();

        final TwksServerConfiguration configuration =
                TwksServerConfiguration.builder()
                        .setFromEnvironment()
                        .set(new ServletContextConfiguration(servletContext)).build();

        final Twks twks = TwksFactory.getInstance().createTwks(configuration.getFactoryConfiguration());

        classpathExtensions = new ClasspathExtensions(configuration.getExtcpDirectoryPath(), twks);
        fileSystemExtensions = new FileSystemExtensions(configuration.getExtfsDirectoryPath(), Optional.of(configuration.getServerBaseUrl()), twks);
        classpathExtensions.initialize();
        fileSystemExtensions.initialize();

        final ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(JerseyServlet.class.getSimpleName(), new JerseyServlet(twks));
        servletRegistration.addMapping("/*");

        logger.info("twks-server " + TwksLibraryVersion.getInstance());

        loadInitialNanopublications(configuration, twks);
    }

    private void loadInitialNanopublications(final TwksServerConfiguration configuration, final Twks twks) {
        final ImmutableList<Nanopublication> nanopublications;
        {
//        NanopublicationParser nanopublicationParser = NanopublicationParser.builder().setDialect(NanopublicationDialect.SPECIFICATION).setLang(Lang.TRIG).setIgnoreMalformedNanopublications(true).build();
            final NanopublicationParser nanopublicationParser = NanopublicationParser.DEFAULT;

            final ImmutableList.Builder<Nanopublication> nanopublicationsBuilder = ImmutableList.builder();

            if (configuration.getInitialNanopublicationsDirectoryPath().isPresent()) {
                nanopublicationsBuilder.addAll(nanopublicationParser.parseDirectory(configuration.getInitialNanopublicationsDirectoryPath().get().toFile()).values());
            }

            if (configuration.getInitialNanopublicationFilePaths().isPresent()) {
                for (final Path nanopublicationFilePath : configuration.getInitialNanopublicationFilePaths().get()) {
                    nanopublicationsBuilder.addAll(nanopublicationParser.parseFile(nanopublicationFilePath.toFile()));
                }
            }

            nanopublications = nanopublicationsBuilder.build();
            if (nanopublications.isEmpty()) {
                return;
            }
        }

        twks.postNanopublications(nanopublications);
        logger.info("posted {} initial nanopublication(s)", nanopublications.size());
    }
}
