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

public class StudyCalendarPostLoadEventListener implements PostLoadEventListener {
    private DaoFinder daoFinder;

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
    private <T extends DomainObject> T findDaoAndLoad(int id, Class<T> klass) {
        DomainObjectDao<T> dao = (DomainObjectDao<T>) daoFinder.findDao(klass);
        return dao.getById(id);
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
