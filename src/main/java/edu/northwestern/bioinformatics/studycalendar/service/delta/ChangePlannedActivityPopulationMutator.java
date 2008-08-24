package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

/**
 * @author Rhett Sutphin
 */
public class ChangePlannedActivityPopulationMutator extends ChangePlannedActivitySimplePropertyMutator {
    private Study study;
    private PopulationDao populationDao;

    public ChangePlannedActivityPopulationMutator(
        PropertyChange change, Study study, ScheduledActivityDao scheduledActivityDao, PopulationDao populationDao
    ) {
        super(change, scheduledActivityDao);
        this.populationDao = populationDao;
        this.study = study;
    }

    @Override
    protected Object getAssignableValue(String value) {
        return value == null ? null : populationDao.getByAbbreviation(study, value);
    }

    @Override
    // TODO: Population changes should be applied to existing schedules, but I don't have time
    // to implement this right now.  RMS20080823
    public boolean appliesToExistingSchedules() {
        return false;
    }
}