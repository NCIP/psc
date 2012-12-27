/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class DeltaDao extends StudyCalendarMutableDomainObjectDao<Delta> implements DeletableDomainObjectDao<Delta> {
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    @Override
    public Class<Delta> domainClass() {
        return Delta.class;
    }

    public <P extends Parent> Delta<P> findDeltaWhereAdded(Child<P> node) {
        return findDeltaForChildChangeOfNode(Add.class, node);
    }

    public <P extends Parent> Delta<P> findDeltaWhereRemoved(Child<P> node) {
        return findDeltaForChildChangeOfNode(Remove.class, node);
    }

    @SuppressWarnings({ "unchecked" })
    private <P extends Parent> Delta<P> findDeltaForChildChangeOfNode(Class<? extends ChildrenChange> changeClass, Child<P> node) {
        List<Delta<P>> deltas = getHibernateTemplate().find(
            String.format(
                "select d from Delta d, %s a where a in elements(d.changesInternal) and d.class = %sDelta and a.childIdText = ? order by d.id desc",
                changeClass.getSimpleName(), node.parentClass().getSimpleName()
            ),
            node.getId().toString()
        );

        log.debug("Found {}", deltas);
        return CollectionUtils.firstElement(deltas);
    }

    @Transactional(readOnly = false)
    public void delete(Delta delta) {
        getHibernateTemplate().delete(delta);
    }

    @Transactional(readOnly = false)
    public void deleteAll(List<Delta> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
