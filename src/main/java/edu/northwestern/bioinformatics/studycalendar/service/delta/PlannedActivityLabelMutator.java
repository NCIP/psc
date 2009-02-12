package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * @author Jalpa Patel
 */
public class PlannedActivityLabelMutator extends AbstractAddAndRemoveMutator {

    public PlannedActivityLabelMutator(ChildrenChange change, DomainObjectDao<? extends Child<?>> dao) {
        super(change, dao);
    }
    @SuppressWarnings({ "unchecked" })

    public void apply(Changeable source) {
      if (!(source instanceof Parent)) {
            throw new StudyCalendarSystemException("You cannot apply an add to a target which does not implement Parent");
        }
        addTo((Parent) source);
    }

    public void revert(Changeable target) {
      if (!(target instanceof Parent)) {
            throw new StudyCalendarSystemException("You cannot apply a remove to a target which does not implement Parent");
        }
        removeFrom((Parent) target);
    }

    @Override
    public boolean appliesToExistingSchedules() {
        return true;
    }
}
