package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Nataliya Shurupova
 */
public class AmendmentDao  extends StudyCalendarDao<Amendment> {
    @Override
    public Class<Amendment> domainClass() {
        return Amendment.class;
    }

    public List<Amendment> getAll() {
        return getHibernateTemplate().find("from Amendment");
    }

    @Transactional(readOnly = false)
    public void save(Amendment amendment) {
        getHibernateTemplate().saveOrUpdate(amendment);
    }

    @Deprecated
    public Amendment getByStudyId(Integer studyId) {
        throw new UnsupportedOperationException("Deprecated");
    }
}
