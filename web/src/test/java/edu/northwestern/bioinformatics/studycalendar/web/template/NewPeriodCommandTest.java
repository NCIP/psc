/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;

/**
 * @author Rhett Sutphin
 */
public class NewPeriodCommandTest extends WebTestCase {
    private NewPeriodCommand command;
    private AmendmentService amendmentService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = registerMockFor(AmendmentService.class);
        command = new NewPeriodCommand(amendmentService, templateService);
    }
    
    public void testApply() throws Exception {
        StudySegment studySegment = new StudySegment();
        command.setStudySegment(studySegment);

        amendmentService.updateDevelopmentAmendment(studySegment, Add.create(command.getPeriod()));
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
