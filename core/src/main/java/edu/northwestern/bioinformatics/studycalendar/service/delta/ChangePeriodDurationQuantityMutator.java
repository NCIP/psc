/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodDurationQuantityMutator extends AbstractChangePeriodDurationMutator {
    private int durationQuanityChange;

    public ChangePeriodDurationQuantityMutator(
        PropertyChange change, TemplateService templateService, ScheduleService scheduleService
    ) {
        super(change, templateService, scheduleService);
        durationQuanityChange = Integer.parseInt(change.getNewValue()) - Integer.parseInt(change.getOldValue());
    }

    @Override
    protected int durationChangeInDays() {
        return getChangedPeriod().getDuration().getUnit().inDays() * durationQuanityChange;
    }
}
