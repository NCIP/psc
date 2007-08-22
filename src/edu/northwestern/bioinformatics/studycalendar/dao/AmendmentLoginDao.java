package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.AmendmentLogin;

import java.util.List;

@Transactional(readOnly = true)
public class AmendmentLoginDao extends StudyCalendarDao<AmendmentLogin> {
        private static final Logger log = LoggerFactory.getLogger(AmendmentLoginDao.class.getName());

    public Class<AmendmentLogin> domainClass() {
        return AmendmentLogin.class;
    }

    public List<AmendmentLogin> getAll() {
        return getHibernateTemplate().find("from AmendmentLogin");
    }

    @Transactional(readOnly = false)
    public void save(AmendmentLogin amendmentLogin) {
        getHibernateTemplate().saveOrUpdate(amendmentLogin);
    }

    public AmendmentLogin getByAmendmentNumber(int number) {
        List<AmendmentLogin> results = getHibernateTemplate().find("from AmendmentLogin a where a.amendmentNumber= ?", number);
        return results.get(0);
    }

    public AmendmentLogin getByStudyId(int studyId) {
        List<AmendmentLogin> results = getHibernateTemplate().find("from AmendmentLogin a where a.studyId= ?", studyId);
        return results.get(0);
    }

}