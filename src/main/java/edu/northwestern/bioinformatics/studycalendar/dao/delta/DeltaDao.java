package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class DeltaDao extends StudyCalendarMutableDomainObjectDao<Delta> {
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    @Override
    public Class<Delta> domainClass() {
        return Delta.class;
    }

    public <P extends PlanTreeNode<?>> Delta<P> findDeltaWhereAdded(PlanTreeNode<P> node) {
        return findDeltaForChildChangeOfNode(Add.class, node);
    }

    public <P extends PlanTreeNode<?>> Delta<P> findDeltaWhereRemoved(PlanTreeNode<P> node) {
        return findDeltaForChildChangeOfNode(Remove.class, node);
    }

    @SuppressWarnings({ "unchecked" })
    private <P extends PlanTreeNode<?>> Delta<P> findDeltaForChildChangeOfNode(Class<? extends ChildrenChange> changeClass, PlanTreeNode<P> node) {
        List<Delta<P>> deltas = getHibernateTemplate().find(
            String.format(
                "select d from Delta d, %s a where a in elements(d.changesInternal) and d.class = %sDelta and a.childId = ? order by d.id desc",
                changeClass.getSimpleName(), node.parentClass().getSimpleName()
            ),
            node.getId()
        );

        log.debug("Found {}", deltas);
        return CollectionUtils.firstElement(deltas);
    }

    public void delete(Delta delta) {
        getHibernateTemplate().delete(delta);
    }
}
