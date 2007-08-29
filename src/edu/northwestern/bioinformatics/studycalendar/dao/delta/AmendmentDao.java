package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Aug 29, 2007
 * Time: 3:41:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class AmendmentDao  extends StudyCalendarDao<Amendment> {
        private static final Logger log = LoggerFactory.getLogger(AmendmentDao.class.getName());

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

    public List<Amendment> getByName (String name) {
        return getHibernateTemplate().find("from Amendment a where a.name= ?", name);
    }

    public List<Amendment> getByDate(String date) {
        return getHibernateTemplate().find("from Amendment a where a.date= ?", date);
    }

    public Amendment getByPreviousAmendmentId(Integer previousAmendmentId) {
        List<Amendment> results = getHibernateTemplate().find("from Amendment a where a.previousAmendment= ?", previousAmendmentId);
        return results.get(0);
    }

    public Amendment getByStudyId(Integer studyId) {
        List<Amendment> results = getHibernateTemplate().find("from Amendment a where a.studyId= ?", studyId);
        return results.get(0);
    }
}
