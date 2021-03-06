package no.osl.cdms.profile.services.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import no.osl.cdms.profile.interfaces.db.TimeMeasurement;
import no.osl.cdms.profile.persistence.MultiContextEntity;
import no.osl.cdms.profile.persistence.ProcedureEntity;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.DurationConverter;

public class TimeMeasurementBucket implements TimeMeasurement {

    private ProcedureEntity procedure;
    private Date timestamp;
    private String duration;
    private int id;

    private DurationConverter converter = ConverterManager.getInstance().getDurationConverter("PT0.123S");

    private List<TimeMeasurement> timeMeasurements;
    private boolean compressed;

    public TimeMeasurementBucket() {
        timeMeasurements = new ArrayList<TimeMeasurement>();
        compressed = false;
    }

    /**
     * Adds a TimeMeasurement to the bucket.
     * The TimeMeasurements are expected to have the same procedure.
     * MultiContext is ignored.
     * @param timeMeasurementEntity
     */
    public void addTimeMeasurement(TimeMeasurement timeMeasurementEntity) {
        timeMeasurements.add(timeMeasurementEntity);
        compressed = false;
    }

    private void compress() {
        if (timeMeasurements == null || timeMeasurements.isEmpty()) {
            return;
        }
        TimeMeasurement timeMeasurement = timeMeasurements.get(0);
        this.procedure = timeMeasurement.getProcedure();
        this.timestamp = timeMeasurement.getTimestamp();
        this.id = timeMeasurement.getId();

        long durationMax = -1;
        for (TimeMeasurement tm: timeMeasurements) {
            long tmVal = converter.getDurationMillis(tm.getDuration());
            if (tmVal > durationMax){
                durationMax = tmVal;
            }
        }
        duration = new Duration(durationMax).toString();
        compressed = true;
    }

    @Override
    public int getId() {
        if (!compressed) {
            compress();
        }
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public ProcedureEntity getProcedure() {
        if (!compressed) {
            compress();
        }
        return procedure;
    }

    @Override
    public int getProcedureId() {
        if (!compressed) {
            compress();
        }
        return procedure.getId();
    }

    @Override
    public void setProcedure(ProcedureEntity procedure) {
        this.procedure = procedure;
    }

    @Override
    public MultiContextEntity getMultiContext() {
        return null;
    }

    /**
     * This setter is ignored: TimeMeasurementBuckets does not use MultiContexts.
     * @param multiContextMeasurement
     */
    @Override
    public void setMultiContext(MultiContextEntity multiContextMeasurement) {
    }

    @Override
    public Date getTimestamp() {
        if (!compressed) {
            compress();
        }
        return timestamp;
    }

    @Override
    public DateTime getJodaTimestamp() {
        if (!compressed) {
            compress();
        }
        return new DateTime(timestamp);
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getDuration() {
        if (!compressed) {
            compress();
        }
        return duration;
    }

    @Override
    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return String.format("ProcedureID=%d, timestamp=%s, duration=%d", getProcedure().getId(), getTimestamp(), getDuration());
    }

    @Override
    public int compareTo(TimeMeasurement other) {
        DurationConverter c = ConverterManager.getInstance().getDurationConverter(this.getDuration());
        return (int)Math.signum(c.getDurationMillis(this.getDuration())-c.getDurationMillis(other.getDuration()));
    }
    

}
