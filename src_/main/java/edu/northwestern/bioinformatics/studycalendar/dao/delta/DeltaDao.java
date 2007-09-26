package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import org.springframework.transaction.annotation.Transactional;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import edu.nwu.bioinformatics.commons.CollectionUtils;

import java.util.List;

@Transactional(readOnly = true)
public class DeltaDao extends StudyCalendarMutableDomainObjectDao<Delta> {
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    @Override
    public Class<Delta> domainClass() {
        return Delta.class;
    }

    @SuppressWarnings({ "unchecked" })
    public <P extends PlanTreeNode<?>> Delta<P> findDeltaWhereAdded(PlanTreeNode<P> node) {
        List<Delta<P>> deltas = getHibernateTemplate().find(
            String.format(
                "select d from Delta d, Add a where a in elements(d.changes) and d.class = %sDelta and a.childId = ?",
                node.parentClass().getSimpleName()
            ),
            node.getId()
        );
        log.debug("Found {}", deltas);
        return CollectionUtils.firstElement(deltas);
    }

}
