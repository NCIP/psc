package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class StudyDao extends StudyCalendarMutableDomainObjectDao<Study> {
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    @SuppressWarnings({ "unchecked" })
    public List<Study> getAll() {
        return getHibernateTemplate().find("from Study");
    }

    @SuppressWarnings({ "unchecked" })
    public List<StudySubjectAssignment> getAssignmentsForStudy(Integer studyId) {
        return getHibernateTemplate().find(
            "select a from StudySubjectAssignment a inner join a.studySite ss inner join a.subject p where ss.study.id = ? order by p.lastName, p.firstName",
            studyId);
    }
}
