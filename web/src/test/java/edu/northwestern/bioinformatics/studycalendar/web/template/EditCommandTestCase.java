/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public abstract class EditCommandTestCase extends StudyCalendarTestCase {
    protected Study study;
    protected Amendment dev;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = new Study();
        study.setPlannedCalendar(new PlannedCalendar());
        dev = new Amendment("dev");
        study.setDevelopmentAmendment(dev);
    }

    protected Delta<?> lastDelta() {
        List<Delta<?>> deltas = study.getDevelopmentAmendment().getDeltas();
        assertPositive("No deltas in dev amendment", deltas.size());
        return deltas.get(deltas.size() - 1);
    }

    protected Change lastChange() {
        List<Change> changes = lastDelta().getChanges();
        assertPositive("No changes in last delta: " + lastDelta(), changes.size());
        return changes.get(changes.size() - 1);
    }
}
