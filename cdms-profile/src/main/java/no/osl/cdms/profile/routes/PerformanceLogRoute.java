package no.osl.cdms.profile.routes;

import no.osl.cdms.profile.parser.DatabaseEntryParser;
import no.osl.cdms.profile.parser.LogLineRegexParser;
import org.apache.camel.builder.RouteBuilder;

import java.util.Map;

public class PerformanceLogRoute extends RouteBuilder {

    public static final String PERFORMANCE_LOG_ROUTE_ID = "PerformanceLogRoute";
    private static final String LOG_DIRECTORY = "C:/data";
    private static final String LOG_FILE = "performance.log";
    private static final int DELAY = 0;

    private LogLineRegexParser logLineRegexParser = new LogLineRegexParser();
    private DatabaseEntryParser databaseEntryParser = new DatabaseEntryParser();

    private static final String LOG_FILE_ENDPOINT = "stream:file?fileName="+LOG_DIRECTORY +"/"+LOG_FILE+"&scanStream=true&scanStreamDelay=" + DELAY;
    private static final String DATABASE_ENDPOINT = "jpa:no.osl.cdms.profile.log.TimeMeasurementEntity";

    @Override
    public void configure() throws Exception{

        //onException(Exception.class)/*.process(exceptionHandler)*/.markRollbackOnly().handled(true);
        from(LOG_FILE_ENDPOINT)
                .convertBodyTo(String.class)                // Converts input to String
                .choice().when(body().isGreaterThan(""))    // Ignores empty lines
                .bean(logLineRegexParser, "parse")          // Parses log entry into String map
                .bean(databaseEntryParser, "parse")         // Parses log entry into database format
                .to(DATABASE_ENDPOINT)                      // Adds log entry to database
                .routeId(PERFORMANCE_LOG_ROUTE_ID);
    }

    /**
     * Test method for viewing parsed log entry
     * Can safely be deleted
     * @param log
     */
    public Map<String, String> printLogEntry(Map<String, String> log) {
        for (String key: log.keySet()) {
            System.out.printf("%s\t\t%s\n", key, log.get(key));
        }
        System.out.println();
        return log;
    }

    public String toString() {
        return PERFORMANCE_LOG_ROUTE_ID;
    }
}