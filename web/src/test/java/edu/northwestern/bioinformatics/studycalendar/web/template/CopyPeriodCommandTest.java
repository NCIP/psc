/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import edu.northwestern.bioinformatics.studycalendar.web.MissingRequiredBoundProperty;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class CopyPeriodCommandTest extends WebTestCase {
    private CopyPeriodCommand command;
    private TemplateDevelopmentService templateDevelopmentService;

    private Period selectedPeriod;
    private StudySegment studySegment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySegment = new StudySegment();
        selectedPeriod = Fixtures.createPeriod(2, 3, 4);
        templateDevelopmentService = registerMockFor(TemplateDevelopmentService.class);

        command = new CopyPeriodCommand(templateService, templateDevelopmentService);
        command.setSelectedPeriod(selectedPeriod);
        command.setStudySegment(studySegment);
    }

    public void testApply() throws Exception {
        Period expectedCopy = new Period();
        expect(templateDevelopmentService.copyPeriod(selectedPeriod, studySegment)).andReturn(expectedCopy);

        replayMocks();
        boolean result = command.apply();
        verifyMocks();

        assertEquals(expectedCopy, command.getPeriod());
        assertTrue("Apply should indicate a redirect to edit", result);
    }

    public void testStudySegmentRequiredInApply() throws Exception {
        try {
            command.setStudySegment(null);
            command.apply();
            fail("Exception not thrown");
        } catch (MissingRequiredBoundProperty missing) {
            assertEquals("studySegment must be provided", missing.getMessage());
        }
    }

    public void testSelectedPeriodRequiredInApply() throws Exception {
        try {
            command.setSelectedPeriod(null);
            command.apply();
            fail("Exception not thrown");
        } catch (MissingRequiredBoundProperty missing) {
            assertEquals("selectedPeriod must be provided", missing.getMessage());
        }
    }
}
