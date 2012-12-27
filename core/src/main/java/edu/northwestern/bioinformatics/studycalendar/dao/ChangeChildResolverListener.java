/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Jalpa Patel
 */
public class ChangeChildResolverListener implements PostLoadEventListener {
    private DaoFinder daoFinder;

    @SuppressWarnings({ "unchecked" })
    public void onPostLoad(PostLoadEvent postLoadEvent) {
        if (postLoadEvent.getEntity() instanceof ChildrenChange) {
            ChildrenChange change = (ChildrenChange) postLoadEvent.getEntity();
            Child child = change.getChild();
            if (child == null) {
                Parent parent = (Parent) change.getDelta().getNode();
                child = (Child) findDaoAndLoad(change.getChildId(), parent.childClass());
            }
            change.setChild(child);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private <T extends DomainObject> T findDaoAndLoad(Integer id, Class<T> klass) {
        if (id != null) {
            DomainObjectDao<T> dao = (DomainObjectDao<T>) daoFinder.findDao(klass);
            return dao.getById(id);
        } else {
            return null;
        }
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
