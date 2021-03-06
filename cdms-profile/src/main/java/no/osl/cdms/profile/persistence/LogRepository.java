package no.osl.cdms.profile.persistence;

import com.google.common.collect.Lists;
import java.util.*;
import javax.persistence.*;

import no.osl.cdms.profile.interfaces.db.MultiContext;
import no.osl.cdms.profile.interfaces.db.Procedure;
import no.osl.cdms.profile.interfaces.db.TimeMeasurement;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

@Repository
public class LogRepository {
    
    private static List<Procedure> cache = Lists.newArrayList();
    private static int inCache(Procedure p){
        return cache.indexOf(p);
    }
    Logger logger = Logger.getRootLogger();

    @PersistenceContext
    private EntityManager entityManager;

    public void persistNewProcedure(Procedure procedure) {
        entityManager.persist((ProcedureEntity)procedure);
    }

    public void persistNewMultiContext(MultiContext multiContext) {
        entityManager.persist((MultiContextEntity)multiContext);
    }

    public void persistNewTimeMeasurement(TimeMeasurement timeMeasurement) {
        entityManager.persist((TimeMeasurementEntity)timeMeasurement);
    }

    public ProcedureEntity getProcedure(int id) {
        return entityManager.find(ProcedureEntity.class, id);
    }

    public MultiContextEntity getMultiContext(int id) {
        return entityManager.find(MultiContextEntity.class, id);
    }

    public TimeMeasurementEntity getTimeMeasurement(int id) {
        return entityManager.find(TimeMeasurementEntity.class, id);
    }

    public List<TimeMeasurement> getTimeMeasurementsByProcedure(DateTime fromDate, DateTime toDate, Procedure procedure) {
        return getTimeMeasurementsByProcedure(fromDate, toDate, procedure, null);
    }

    public List<TimeMeasurement> getTimeMeasurementsByProcedure(DateTime fromDate, DateTime toDate, Procedure procedure, TimeMeasurement.Field orderBy) {        
        TypedQuery<TimeMeasurement> query = entityManager.createQuery("SELECT a FROM TimeMeasurementEntity a " +
                "where a.procedure = :procedure AND a.timestamp >= :fromDate AND a.timestamp <= :toDate"
                + queryOrderingSuffix(orderBy), TimeMeasurement.class);
        query.setParameter("procedure", procedure);
        query.setParameter("fromDate", fromDate.toDate());
        query.setParameter("toDate", toDate.toDate());

        return query.getResultList();
    }

    public List<TimeMeasurement> getTimeMeasurementsByProcedure(Procedure procedure) {
        return getTimeMeasurementsByProcedure(procedure, null);
    }

    public List<TimeMeasurement> getTimeMeasurementsByProcedure(Procedure procedure, TimeMeasurement.Field orderBy) {
        TypedQuery<TimeMeasurement> query = entityManager.createQuery(
                "SELECT a FROM TimeMeasurementEntity a where a.procedure = :procedure" + queryOrderingSuffix(orderBy),
                TimeMeasurement.class);
        query.setParameter("procedure", procedure);

        return query.getResultList();
    }
    public Procedure getEqualPersistedProcedure(Procedure procedure) {
        int cached = inCache(procedure);
        if (cached != -1){
            return cache.get(cached);
        }
        logger.error("Cache miss for: "+procedure);
        
        TypedQuery<ProcedureEntity> query = entityManager.createQuery(
                "SELECT a FROM ProcedureEntity a where a.className = :class AND " +
                        "a.method = :method", ProcedureEntity.class);
        query.setParameter("class", procedure.getClassName());
        query.setParameter("method", procedure.getMethod());
        try {
            Procedure db =  query.getSingleResult();
            cache.add(db);
            return db;
        } catch (javax.persistence.NoResultException e) {
            return procedure;
        }
    }

    public List<ProcedureEntity> getAllProcedures() {
        TypedQuery<ProcedureEntity> query = entityManager.createQuery(
                "SELECT a FROM ProcedureEntity a", ProcedureEntity.class);
        try {
            return query.getResultList();
        } catch (javax.persistence.NoResultException e) {
            return new java.util.ArrayList<ProcedureEntity>();
        }

    }

    public TimeMeasurement getLatestTimeMeasurement() {
        TypedQuery<TimeMeasurement> query = entityManager.createQuery(
                "SELECT a FROM TimeMeasurementEntity a where a.timestamp = (SELECT MAX(b.timestamp) from TimeMeasurementEntity b)",
                TimeMeasurement.class);
        query.setMaxResults(1);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public TimeMeasurement getOldestTimeMeasurement() {
        TypedQuery<TimeMeasurement> query = entityManager.createQuery(
                "SELECT a FROM TimeMeasurementEntity a where a.timestamp = (SELECT MIN(b.timestamp) from TimeMeasurementEntity b)",
                TimeMeasurement.class);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void deleteOldLogEntries() {
        Date deleteTime = new DateTime().minusDays(15).toDate();

        // Get old TimeMeasurements
        TypedQuery<TimeMeasurement> multiContextQuery = entityManager.createQuery(
                "SELECT a FROM TimeMeasurementEntity a WHERE a.timestamp < :deleteTime",
                TimeMeasurement.class);
        multiContextQuery.setParameter("deleteTime", deleteTime);
        List<TimeMeasurement> timeMeasurements = multiContextQuery.getResultList();


        for (TimeMeasurement timeMeasurement: timeMeasurements) {
            Query deleteQuery;

            // Delete multi context if exists
            if (timeMeasurement.getMultiContext() != null) {
                deleteQuery = entityManager.createQuery("DELETE FROM MultiContextEntity a WHERE a.id = :id");
                deleteQuery.setParameter("id", timeMeasurement.getMultiContext().getId());
                deleteQuery.executeUpdate();
            }

            // Delete time measurement
            deleteQuery = entityManager.createQuery("DELETE FROM TimeMeasurementEntity a WHERE a.id = :id");
            deleteQuery.setParameter("id", timeMeasurement.getId());
            deleteQuery.executeUpdate();
        }

    }

    public void deleteUnusedProcedures() {
        List<ProcedureEntity> procedures = getAllProcedures();
        List<ProcedureEntity> unusedProcedures = new ArrayList<ProcedureEntity>(procedures);
        String deletedProcedures = "";

        // Find unused procedures
        for (Procedure procedure : procedures) {
            TypedQuery<TimeMeasurement> query = entityManager.createQuery(
                    "SELECT a FROM TimeMeasurementEntity a WHERE a.procedure = :procedure",
                    TimeMeasurement.class);
            query.setParameter("procedure", procedure);
            try {
                if (!query.getResultList().isEmpty()) {
                    unusedProcedures.remove(procedure);
                }
            } catch (NoResultException e) {
                // Do nothing, the procedure is kept in the delete list
            }
        }

        if (unusedProcedures.isEmpty()) {
            return;
        }

        // Delete unused procedures
        for (Procedure procedure : unusedProcedures) {
            Query deleteQuery = entityManager.createQuery("DELETE FROM ProcedureEntity a WHERE a.id = :id ");
            deleteQuery.setParameter("id", procedure.getId());
            deleteQuery.executeUpdate();
            deletedProcedures += "\t" + procedure.toString() + "\n";
        }
        logger.info("Deleted following unused procedures: \n" + deletedProcedures);
    }

    /**
     * Generates a String to be appended to a query.
     * The String will specify which field to order by, if any.
     * @param orderBy
     * @return query suffix
     */
    private String queryOrderingSuffix(TimeMeasurement.Field orderBy) {
        String querySuffix = "";
        if (orderBy == null) {
            return querySuffix;
        }
        switch (orderBy) {
            case DURATION:
                querySuffix = " ORDER BY a.duration";
                break;
            case TIMESTAMP:
                querySuffix = " ORDER BY a.timestamp";
                break;
        }
        return querySuffix;
    }
}
