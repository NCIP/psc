package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class StudyDao extends StudyCalendarMutableDomainObjectDao<Study> implements DeletableDomainObjectDao<Study> {

    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    /**
     * Finds all the studies available
     *
     * @return      a list of all the available studies
     */
    @SuppressWarnings( { "unchecked" })
    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }

    /**
     * Finds the assignments for a particular study
     *
     * @param  studyId the study to get the assignments for
     * @return      a list of the assignments for the study id given
     */
    @SuppressWarnings( { "unchecked" })
    public List<StudySubjectAssignment> getAssignmentsForStudy(final Integer studyId) {
        return getHibernateTemplate()
            .find("select a from StudySubjectAssignment a inner join a.studySite ss inner join a.subject p where ss.study.id = ? order by p.lastName, p.firstName",
                studyId);
    }

    @Deprecated // use getByAssignedIdentifier
    public Study getStudyByAssignedIdentifier(final String assignedIdentifier) {
        return getByAssignedIdentifier(assignedIdentifier);
    }

    /**
     * Finds a study based on the assignment identifer given
     *
     * @param  assignedIdentifier the assignment identifier for the study to search for
     * @return      the study that has the given assignment identifier
     */
    @SuppressWarnings({"unchecked"})
    public Study getByAssignedIdentifier(String assignedIdentifier) {
        List<Study> results = getHibernateTemplate().find("from Study where assignedIdentifier= ?", assignedIdentifier);
        return CollectionUtils.firstElement(results);
    }

    /**
     * Gets the study id that corresponds to the given assignment identifier
     *
     * @param  assignedIdentifier the assignment identifier to search for the study to search for
     * @return      the study id that corresponds to the study assignment identifier given
     */
    public String getStudyIdByAssignedIdentifier(final String assignedIdentifier) {
        Object studyId = getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createSQLQuery(
                        "select max(id) from studies where  assigned_identifier='" + assignedIdentifier + "'")
                        .uniqueResult();
            }
        });


        if (studyId != null)
            return studyId.toString();
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public String getNewStudyName() {
        String templateName = "[ABC 1000]";
        List<Study> studies = getHibernateTemplate().find("from Study a where assigned_identifier LIKE '[ABC %' ORDER BY assigned_identifier DESC");
        if (studies.size() == 0) {
            return templateName;
        }
        Study study = studies.get(0);
        String studyName = study.getName();
        String numericPartSupposedly = studyName.substring(studyName.indexOf(" ")+1, studyName.lastIndexOf("]"));
        int newNumber = 1000;
        try {
            newNumber = new Integer(numericPartSupposedly)+1;
        }catch(NumberFormatException e) {
            log.debug("Can't convert study's numeric string " + newNumber + " into int");
        }
        templateName = "[ABC " + newNumber + "]";
        return templateName;
    }


    /**
     * Deletes a study
     *
     * @param  study the study to be deleted
     */
    @Transactional(readOnly = false)
    public void delete(Study study) {
        getHibernateTemplate().delete(study);
    }
}
