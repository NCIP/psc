package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.AmendmentLogin;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;

import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;

@Transactional(readOnly = true)

public class DeltaDao extends StudyCalendarDao<Delta> {
        private static final Logger log = LoggerFactory.getLogger(DeltaDao.class.getName());

    public Class<Delta> domainClass() {
        return Delta.class;
    }

    public List<Delta> getAll() {
        return getHibernateTemplate().find("from Delta");
    }

    @Transactional(readOnly = false)
    public void save(Delta delta) {
        getHibernateTemplate().saveOrUpdate(delta);
    }

//    public Delta getByChangeId(int changeId) {
//        List<Delta> results = getHibernateTemplate().find("from Delta a where a.changeId= ?", changeId);
//        return results.get(0);
//    }

}

//public abstract class DeltaDao <T extends PlanTreeNode<?>> extends StudyCalendarDao<T> {
//    public List<T> getAll() {
//        return getHibernateTemplate().find("from Delta");
//    }
//
//    @Transactional(readOnly = false)
//    public void save(T delta) {
//        getHibernateTemplate().saveOrUpdate(delta);
//    }
//
//    public Delta getByChangeId(int changeId) {
//        List<Delta> results = getHibernateTemplate().find("from Delta a where a.changeId= ?", changeId);
//        return results.get(0);
//    }
//}
