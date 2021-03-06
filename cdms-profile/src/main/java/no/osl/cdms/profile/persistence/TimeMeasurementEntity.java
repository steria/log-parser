package no.osl.cdms.profile.persistence;

import java.util.Date;
import java.util.logging.Logger;
import javax.persistence.*;
import no.osl.cdms.profile.interfaces.db.TimeMeasurement;
import org.joda.time.DateTime;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.DurationConverter;

@Entity
@Table(name = "CDM_PROFILE_TIMEMEASUREMENT")
public class TimeMeasurementEntity implements TimeMeasurement {

    @Column(name = "TIMEMEASUREMENT_ID")
    @SequenceGenerator(name = "TIMEMEASUREMENT_SEQ_GEN", sequenceName = "TIMEMEASUREMENT_SEQ")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TIMEMEASUREMENT_SEQ_GEN")
    private int id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "PROCEDURE_ID")
    private ProcedureEntity procedure;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "MULTICONTEXT_ID")
    private MultiContextEntity multiContext;

    @Column(name = "TIMESTAMP")
    private Date timestamp;

    @Column(name = "DURATION")
    private String duration;

    public TimeMeasurementEntity() {

    }

    public TimeMeasurementEntity(ProcedureEntity procedure, MultiContextEntity multiContext, Date timestamp,
                                 String duration) {
        setProcedure(procedure);
        setMultiContext(multiContext);
        this.timestamp = timestamp;
        this.duration = duration;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public ProcedureEntity getProcedure() {
        return procedure;
    }

    @Override
    public int getProcedureId() {
        return procedure.getId();
    }

    @Override
    public void setProcedure(ProcedureEntity procedure) {
        this.procedure = procedure;
    }

    @Override
    public MultiContextEntity getMultiContext() {
        return multiContext;
    }

    @Override
    public void setMultiContext(MultiContextEntity multiContextMeasurement) {
        this.multiContext = multiContextMeasurement;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public DateTime getJodaTimestamp() {
        return new DateTime(timestamp);
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getDuration() {
        return duration;
    }

    @Override
    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(TimeMeasurement other) {
//        long thisTime = this.getJodaTimestamp().getMillis();
//        long otherTime = other.getJodaTimestamp().getMillis();
        DurationConverter c = ConverterManager.getInstance().getDurationConverter(this.getDuration());
        return (int)Math.signum(c.getDurationMillis(this.getDuration())-c.getDurationMillis(other.getDuration()));
//        return (int) Math.signum(thisTime - otherTime);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.id;
        hash = 23 * hash + (this.procedure != null ? this.procedure.hashCode() : 0);
        hash = 23 * hash + (this.multiContext != null ? this.multiContext.hashCode() : 0);
        hash = 23 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
        hash = 23 * hash + (this.duration != null ? this.duration.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimeMeasurementEntity other = (TimeMeasurementEntity) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.procedure != other.procedure && (this.procedure == null || !this.procedure.equals(other.procedure))) {
            return false;
        }
        if (this.multiContext != other.multiContext && (this.multiContext == null || !this.multiContext.equals(other.multiContext))) {
            return false;
        }
        if ((this.timestamp == null) ? (other.timestamp != null) : !this.timestamp.equals(other.timestamp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TimeMeasurementEntity{" + "id=" + id + ", procedure=" + procedure + ", multiContext=" + multiContext + ", timestamp=" + timestamp + ", duration=" + duration + '}';
    }
}
