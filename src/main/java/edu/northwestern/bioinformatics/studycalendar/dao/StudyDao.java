package edu.northwestern.bioinformatics.studycalendar.dao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.nwu.bioinformatics.commons.CollectionUtils;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class StudyDao extends StudyCalendarMutableDomainObjectDao<Study> {

    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    @SuppressWarnings( { "unchecked" })
    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }

    @SuppressWarnings( { "unchecked" })
    public List<StudySubjectAssignment> getAssignmentsForStudy(final Integer studyId) {
        return getHibernateTemplate()
            .find("select a from StudySubjectAssignment a inner join a.studySite ss inner join a.subject p where ss.study.id = ? order by p.lastName, p.firstName",
                studyId);
    }

    public Study getByAssignedIdentifier(final String assignedIdentifier) {
        List<Study> results = getHibernateTemplate().find("from Study where assignedIdentifier= ?", assignedIdentifier);
        return CollectionUtils.firstElement(results);
    }

    public void commitInProgressStudy(final String ccIdentifier) {
        Object studyId = getSession().createSQLQuery(
            "select id from studies where load_status=0 and assigned_identifier='" + ccIdentifier + "'")
            .uniqueResult();

        if (studyId == null) {
            String msg = "Either no study exist with ccIdentifier :" + ccIdentifier
                + " or study is already been commited";
            logger.error(msg);
            throw new StudyCalendarSystemException(msg);
        }
        logger.info("For cc identifier:" + ccIdentifier + " found study:id-" + studyId);

        // update load status of the study
        getSession().createSQLQuery("update studies set load_status=1 where id=" + studyId.toString()).executeUpdate();
        logger.info("commited study:id:" + studyId);
    }

    public void deleteInprogressStudy(final String ccIdentifier) {
        Object studyId = getSession().createSQLQuery(
            "select id from studies where load_status=0 and assigned_identifier='" + ccIdentifier + "'")
            .uniqueResult();

        if (studyId == null) {
            String message = "Either no study exist with ccIdentifier : " + ccIdentifier
                + " or study is already commited";
            logger.error(message);
            throw new StudyCalendarSystemException(message);
        }
        logger.info("For cc identifier:" + ccIdentifier + " found study:id=" + studyId);

        // delete study
        getSession().createSQLQuery("delete from planned_calendars where study_id=" + studyId.toString())
            .executeUpdate();
        getSession().createSQLQuery("delete from study_sites where study_id=" + studyId.toString()).executeUpdate();

        getSession().createSQLQuery("delete from studies where id=" + studyId.toString()).executeUpdate();
        logger.info("deleted study:id:" + studyId);
    }
}
