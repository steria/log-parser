package no.osl.cdms.profile.routes;

import no.osl.cdms.profile.api.TimeMeasurement;
import no.osl.cdms.profile.factories.EntityFactory;
import no.osl.cdms.profile.log.LogRepository;
import no.osl.cdms.profile.log.TimeMeasurementEntity;
import no.osl.cdms.profile.parser.LogLineRegexParser;
import no.osl.cdms.profile.utilities.GuavaHelpers;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

public class PerformanceLogRoute extends RouteBuilder {

    public static final String PERFORMANCE_LOG_ROUTE_ID = "PerformanceLogRoute";
    private static final String LOG_DIRECTORY = "data/log";
    private static final String LOG_FILE = "performance.log";
    private static final int DELAY = 0;

    private static final String LOG_FILE_ENDPOINT = "stream:file?fileName=%s/%s&scanStream=true&scanStreamDelay=%d";
    private static final String DATABASE_ENDPOINT = "jpa:%s?usePersist=true";

    @Autowired
    private EntityFactory entityFactory;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogLineRegexParser logLineRegexParser;

    @Autowired
    private GuavaHelpers guavaHelpers;

    public PerformanceLogRoute() {

    }

    public PerformanceLogRoute(EntityFactory entityFactory, LogLineRegexParser logLineRegexParser) {
        this.entityFactory = entityFactory;
        this.logLineRegexParser = logLineRegexParser;
    }

    @Override
    public void configure() throws Exception{
        final TimeMeasurement lastInsertedTimeMeasurement = logRepository.getLatestTimeMeasurement();

        fromF(LOG_FILE_ENDPOINT, LOG_DIRECTORY, LOG_FILE, DELAY)
                .convertBodyTo(String.class)                  // Converts input to String
                .choice().when(body().isGreaterThan(""))      // Ignores empty lines
                .choice().when(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                DateTime logEntryDate = new DateTime(guavaHelpers.parseDateString(exchange.getIn().getBody().toString().substring(0,23)));
                if (lastInsertedTimeMeasurement == null) {
                    return true;
                } else if (logEntryDate.isAfter(lastInsertedTimeMeasurement.getJodaTimestamp()) ||
                        logEntryDate.isEqual(lastInsertedTimeMeasurement.getJodaTimestamp())) {
                    return true;
                }
                return false;
            }
        })
                .bean(logLineRegexParser, "parse")            // Parses log entry into String map
                .bean(entityFactory, "createTimemeasurement") // Parses log entry into database format
                .split(body())
                .choice().when(body().isNotNull())
                .toF(DATABASE_ENDPOINT, TimeMeasurementEntity.class.getCanonicalName())
                .routeId(PERFORMANCE_LOG_ROUTE_ID);
    }

    public String toString() {
        return PERFORMANCE_LOG_ROUTE_ID;
    }
}
