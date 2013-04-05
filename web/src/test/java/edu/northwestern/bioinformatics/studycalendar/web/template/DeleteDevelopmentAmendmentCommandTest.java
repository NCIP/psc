/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;

/**
 * @author Rhett Sutphin
 */
public class DeleteDevelopmentAmendmentCommandTest extends StudyCalendarTestCase {
    private DeleteDevelopmentAmendmentCommand command;

    private Study study;
    private AmendmentService amendmentService;
    private TemplateDevelopmentService tempDevService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = new Study();
        study.setDevelopmentAmendment(new Amendment());

        amendmentService = registerMockFor(AmendmentService.class);

        tempDevService = registerMockFor(TemplateDevelopmentService.class);

        command = new DeleteDevelopmentAmendmentCommand(amendmentService, tempDevService);
        command.setStudy(study);
    }
    
    public void testApply() throws Exception {
//        amendmentService.deleteDevelopmentAmendment(study);
        tempDevService.deleteDevelopmentAmendment(study);
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
