/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.osl.cdms.profile.utilities;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import no.osl.cdms.profile.api.Procedure;
import no.osl.cdms.profile.api.TimeMeasurement;
import no.osl.cdms.profile.factories.EntityFactory;
import no.osl.cdms.profile.log.ProcedureEntity;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.DurationConverter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author nutgaard
 */
public class GuavaHelpers {

    private static DurationConverter converter = ConverterManager.getInstance().getDurationConverter("PT0.012S");

    public static Predicate<Map.Entry<String, String>> isDuration() {
        return new Predicate<Map.Entry<String, String>>() {
            @Override
            public boolean apply(Map.Entry<String, String> input) {
                if (input == null || input.getKey() == null || input.getValue() == null) {
                    return false;
                }
                return input.getKey().endsWith("duration") && !input.getValue().equalsIgnoreCase("null");
            }
        };
    }

    public static Function<Map.Entry<String, String>, TimeMeasurement> getConverter(final Map<String, String> properties) {
        return new Function<Map.Entry<String, String>, TimeMeasurement>() {
            @Override
            public TimeMeasurement apply(Map.Entry<String, String> input) {
                String[] measured = parseKey(input.getKey(), properties);
                String time = parseDuration(input.getValue());

                Procedure m = (ProcedureEntity) EntityFactory.getInstance().createProcedure(measured[0], measured[1]);
                TimeMeasurement tm = EntityFactory.getInstance().createTimeMeasurement(m, parseDateString(properties.get("timestamp")), time);

                return tm;
            }
        };
    }

    public static Date parseDateString(String string) {

        try {
            Date date = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss,SSS", Locale.ENGLISH).parse(string);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd kk:mm:ss,SSS");
        Date date = dtf.parseDateTime(string).toDate();

        return date;
    }

    public static String[] parseKey(String key, Map<String, String> map) {
        if (key == null) {
            throw new IllegalArgumentException("Key for timemeasurement cannot be <null>");//Should not be possible, cannot store null as key in maps
        }
        if (key.startsWith("LocalThread")) {
            String id = map.get("LocalThreadContext.id");
            if (id == null) {
                throw new NullPointerException("LocalThreadContext.id was not found within this object");
            }
            String[] out = id.split("\\.");
            return out;
        } else {
            String[] parts = key.split("[\\.:]");
            if (parts.length < 3) {
                throw new NullPointerException("Unmarshalling of map to timemeasure failed, too few key parts: " + key);
            }
            if (parts[1].equals("Total") || parts[1].equals("Wait")) {
                return new String[]{parts[1], ""};
            } else {
                return new String[]{parts[2], parts[3]};
            }
        }
    }

    public static String parseDuration(String duration) {
        try {
            long ans = converter.getDurationMillis(duration);
            return duration;
        } catch (Exception ex) {
            try {
                double d = Double.parseDouble(duration);
                return "PT" + (d / 1000) + "S";
            } catch (Exception e) {
                throw new IllegalArgumentException("Duration " + duration + " was not recognized");
            }
        }
    }
}