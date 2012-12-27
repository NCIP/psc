/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodDurationUnitMutator extends AbstractChangePeriodDurationMutator {
    private Boolean isThereAChange;
    private int changeInDays;

    public ChangePeriodDurationUnitMutator(PropertyChange change, TemplateService templateService, ScheduleService scheduleService) {
        super(change, templateService, scheduleService);
        Duration.Unit old = Duration.Unit.valueOf(change.getOldValue());
        Duration.Unit nu  = Duration.Unit.valueOf(change.getNewValue());
        if (old == nu) {
            isThereAChange = null;
        } else if (old == null || nu == null) {
            log.error("Invalid change {} in revision {}.  Skipping.", change, change.getDelta().getRevision());
            isThereAChange = null;
        } else {
            isThereAChange = true;
            changeInDays = nu.inDays() - old.inDays();
        }
    }

    @Override
    protected int durationChangeInDays() {
        if (isThereAChange == null) return 0;
        return getChangedPeriod().getDuration().getQuantity() * changeInDays;
    }
}
