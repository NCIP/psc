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
    public Class<Study> domainClass() {
        return Study.class;
    }

    @Transactional(readOnly = false)
    public void save(Study study) {
        getHibernateTemplate().saveOrUpdate(study);
    }

    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }

    public List<StudyParticipantAssignment> getAssignmentsForStudy(Integer studyId) {
        return getHibernateTemplate().find(
            "select a from StudyParticipantAssignment a inner join a.studySite ss inner join a.participant p where ss.study.id = ? order by p.lastName, p.firstName", 
            studyId);
    }
    
    public Study getById(int id) {
        return (Study) getHibernateTemplate().get(Study.class, new Integer(id));
    }
}
