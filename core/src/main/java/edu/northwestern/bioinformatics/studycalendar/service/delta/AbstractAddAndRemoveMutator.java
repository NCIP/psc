/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
abstract class AbstractAddAndRemoveMutator implements Mutator {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ChildrenChange change;
    protected DomainObjectDao<? extends Child<?>> dao;

    public AbstractAddAndRemoveMutator(ChildrenChange change, DomainObjectDao<? extends Child<?>> dao) {
        this.dao = dao;
        this.change = change;
        if (dao == null && this.change.getChild() == null) {
            log.warn("{} for {} has neither a DAO nor a concrete child.  Unless this changes by the time the change is applied, there will be an exception.",
                getClass().getName(), change);
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    protected Child findChild() {
        if (change.getChild() != null) {
            return change.getChild();
        } else {
            return (Child) dao.getById(change.getChildId());
        }
    }

    // accessor for testing
    DomainObjectDao<? extends Child> getDao() { return dao; }

    protected <C extends Child> void addTo(Parent<C, ?> source) {
        C child = (C) findChild();
        if (source.isMemoryOnly()) {
            child = (C) child.transientClone();
        }
        log.debug("Adding {} to {}", child, source);
        source.addChild(child);
    }

    protected <C extends Child> void removeFrom(Parent<C, ?> target) {
        C toRemove = null;
        for (C child : target.getChildren()) {
            if (change.isSameChild(child)) {
                log.debug("Removing {} from {}", child, target);
                toRemove = child;
                break;
            }
        }
        if (toRemove == null) {
            log.warn("The child referenced in {} was not found in {}", change, target);
            return;
        }
        target.removeChild(toRemove);
    }

    public boolean appliesToExistingSchedules() {
        return false;
    }

    public void apply(ScheduledCalendar calendar) {
        throw new StudyCalendarSystemException("%s cannot be applied to an existing schedule", change);
    }
}
