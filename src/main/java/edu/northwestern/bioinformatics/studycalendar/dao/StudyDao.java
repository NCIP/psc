package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
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
     * @return a list of all the available studies
     */
    @SuppressWarnings({"unchecked"})
    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }

    /**
     * Finds the assignments for a particular study
     *
     * @param studyId the study to get the assignments for
     * @return a list of the assignments for the study id given
     */
    @SuppressWarnings({"unchecked"})
    public List<StudySubjectAssignment> getAssignmentsForStudy(final Integer studyId) {
        return getHibernateTemplate()
                .find("select a from StudySubjectAssignment a inner join a.studySite ss inner join a.subject p where ss.study.id = ? order by p.lastName, p.firstName",
                        studyId);
    }

    @Deprecated
    // use getByAssignedIdentifier
    public Study getStudyByAssignedIdentifier(final String assignedIdentifier) {
        return getByAssignedIdentifier(assignedIdentifier);
    }

    /**
     * Finds a study based on the assignment identifer given
     *
     * @param assignedIdentifier the assignment identifier for the study to search for
     * @return the study that has the given assignment identifier
     */
    @SuppressWarnings({"unchecked"})
    public Study getByAssignedIdentifier(String assignedIdentifier) {
        List<Study> results = getHibernateTemplate().find("from Study where assignedIdentifier= ?", assignedIdentifier);
        return CollectionUtils.firstElement(results);
    }

    /**
     * Gets the study id that corresponds to the given assignment identifier
     *
     * @param assignedIdentifier the assignment identifier to search for the study to search for
     * @return the study id that corresponds to the study assignment identifier given
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
        List<Study> studies = getHibernateTemplate().find("from Study a where assigned_identifier LIKE '[ABC %]' ORDER BY assigned_identifier DESC");
        Collections.sort(studies, new StudyTemporaryNameComparator());
        if (studies.size() == 0) {
            return templateName;
        }
        Study study = studies.get(0);
        String studyName = study.getName();
        String numericPartSupposedly = studyName.substring(studyName.indexOf(" ") + 1, studyName.lastIndexOf("]"));
        int newNumber = 1000;
        try {
            newNumber = new Integer(numericPartSupposedly) + 1;
        } catch (NumberFormatException e) {
            log.debug("Can't convert study's numeric string " + newNumber + " into int");
        }
        templateName = "[ABC " + newNumber + "]";
        return templateName;
    }


    /**
     * Deletes a study
     *
     * @param study the study to be deleted
     */
    @Transactional(readOnly = false)
    public void delete(Study study) {
        getHibernateTemplate().delete(study);
    }

    /**
     * Search studies by matching study name (or assigned identifier of study) to search text.
     * Returns all studies if no study found matching with given search text
     *
     * @param studySearchText
     * @return
     */
    public List<Study> searchStudiesByStudyName(final String studySearchText) {

        String searchText = "%" + studySearchText.toLowerCase() + "%";
        List<Study> studies = getHibernateTemplate().find("from Study s where lower(s.assignedIdentifier) LIKE ? ORDER BY s.assignedIdentifier DESC ", searchText);

        if (studies.isEmpty()) {
            return getAll();
        }
        return studies;
    }

    private static class StudyTemporaryNameComparator implements Comparator<Study> {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        /**
         * Compares the study name. Compares only  studies having name matches with [ABC *] .
         *
         * @param study
         * @param anotherStudy
         * @return
         */
        public int compare(final Study study, final Study anotherStudy) {
            // String numericPartSupposedly = "";
            String name = study.getName();
            String anotherStudyName = anotherStudy.getName();
            if (name.indexOf("ABC") <= 0 || anotherStudyName.indexOf("ABC") <= 0) {
                return 1;
            } else if (name.indexOf("]") <= 0 || anotherStudyName.indexOf("]") <= 0) {
                return 1;
            }
            if (name.indexOf("[") < 0 || anotherStudyName.indexOf("[") < 0) {
                return 1;
            }

            try {
                String numericPartSupposedly = name.substring(name.indexOf(" ") + 1, name.lastIndexOf("]"));
                String anotherNumericPartSupposedly = anotherStudyName.substring(anotherStudyName.indexOf(" ") + 1, anotherStudyName.lastIndexOf("]"));
                Integer number = new Integer(numericPartSupposedly);
                Integer anotherNumber = new Integer(anotherNumericPartSupposedly);
                return anotherNumber.compareTo(number);

            } catch (NumberFormatException e) {
                logger.debug("error while comparing two stduies. first study name:" + study.getName() + " another study name:" + anotherStudy.getName());
            }

            return 1;
        }
    }
}
