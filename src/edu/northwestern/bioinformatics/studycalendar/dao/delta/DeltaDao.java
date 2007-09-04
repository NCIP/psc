package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

import java.util.List;

@Transactional(readOnly = true)
public class DeltaDao extends StudyCalendarDao<Delta> {
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    @Override
    public Class<Delta> domainClass() {
        return Delta.class;
    }

    @Transactional(readOnly = false)
    public void save(Delta<?> delta) {
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
