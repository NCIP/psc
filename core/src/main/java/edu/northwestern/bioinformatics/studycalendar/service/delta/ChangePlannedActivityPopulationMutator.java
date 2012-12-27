/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import java.util.List;
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
        Population population = null;
        if(value != null) {
            population = populationDao.getByAbbreviation(study,value);
            if(population!=null) {
                return population;
            } else {
                for(Delta delta:study.getDevelopmentAmendment().getDeltas()) {
                   if(delta.getNode() instanceof Study) {
                       List<ChildrenChange> changes = delta.getChanges();
                       for(ChildrenChange change:changes  ) {
                           if ((ChangeAction.ADD).equals(change.getAction())) {
                                    population  = populationDao.getById(change.getChildId());
                                    if(value.equals(population.getAbbreviation()))
                                        return population;
                           }
                       }
                   }
               }
            }
        }
        return population;
    }

    @Override
    // TODO: Population changes should be applied to existing schedules, but I don't have time
    // to implement this right now.  RMS20080823
    public boolean appliesToExistingSchedules() {
        return false;
    }
}