/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;

import java.util.List;

public class ChangeDao extends StudyCalendarMutableDomainObjectDao<Change> implements DeletableDomainObjectDao<Change> {
    @Override
    public Class<Change> domainClass() {
        return Change.class;
    }

    public void delete(Change change) {
        getHibernateTemplate().delete(change);
    }

    public void deleteAll(List<Change> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
