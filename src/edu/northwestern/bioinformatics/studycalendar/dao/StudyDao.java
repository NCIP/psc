package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class StudyDao extends StudyCalendarGridIdentifiableDao<Study> {
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    @Transactional(readOnly = false)
    public void save(Study study) {
        getHibernateTemplate().saveOrUpdate(study);
    }

    @SuppressWarnings({ "unchecked" })
    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }

    @SuppressWarnings({ "unchecked" })
    public List<StudyParticipantAssignment> getAssignmentsForStudy(Integer studyId) {
        return getHibernateTemplate().find(
            "select a from StudyParticipantAssignment a inner join a.studySite ss inner join a.participant p where ss.study.id = ? order by p.lastName, p.firstName", 
            studyId);
    }
}
