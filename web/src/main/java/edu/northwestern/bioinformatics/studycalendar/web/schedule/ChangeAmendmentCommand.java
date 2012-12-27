/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ChangeAmendmentCommand {
    private Map<Amendment, Boolean> amendments;
    private StudySubjectAssignment assignment;
    private DeltaService deltaService;

    public ChangeAmendmentCommand(StudySubjectAssignment assignment, DeltaService deltaService) {
        this.assignment = assignment;
        this.deltaService = deltaService;

        amendments = new LinkedHashMap<Amendment, Boolean>();
        for (Amendment amendment : assignment.getAvailableUnappliedAmendments()) {
            amendments.put(amendment, false);
        }
    }

    public void apply() {
        for (Map.Entry<Amendment, Boolean> entry : getAmendments().entrySet()) {
            if (entry.getValue()) {
                deltaService.amend(assignment, entry.getKey());
            }
        }
    }

    ////// BEAN PROPERTIES

    public Map<Amendment, Boolean> getAmendments() {
        return amendments;
    }

    public StudySubjectAssignment getAssignment() {
        return assignment;
    }
}
