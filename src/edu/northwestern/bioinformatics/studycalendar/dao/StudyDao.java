package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import gov.nih.nci.security.authorization.domainobjects.Group;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class StudyDao extends WithBigIdDao<Study> {
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
        return getHibernateTemplate().find("select a from StudyParticipantAssignment a inner join a.studySite ss where ss.study.id = ?", studyId);
    }
    
    public Study getById(int id) {
        return (Study) getHibernateTemplate().get(Study.class, new Integer(id));
    }
}
