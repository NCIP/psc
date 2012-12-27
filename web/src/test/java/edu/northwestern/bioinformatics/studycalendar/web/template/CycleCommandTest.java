/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class CycleCommandTest extends StudyCalendarTestCase {
    private StudySegment studySegment;
    private CycleCommand command;
    private AmendmentService amendmentService;
    private TemplateService templateService;
    private Study study;

    public void setUp()  throws Exception  {
        super.setUp();
        int id = 44;
        studySegment = Fixtures.setId(id, Fixtures.createNamedInstance("StudySegment", StudySegment.class));
        studySegment.setCycleLength(10);
        amendmentService = registerMockFor(AmendmentService.class);
        templateService = registerMockFor(TemplateService.class);
        command = new CycleCommand(templateService,amendmentService);
        command.setStudySegment(studySegment);
    }

    public void testApplySetsCycleLength () throws Exception  {
        command.apply();
        assertEquals("Cycle length not set",(Integer)10, command.getStudySegment().getCycleLength());
    }

    public void testApplyCycleLengthChange() throws Exception {
        command.setCycleLength(12);
        expect(amendmentService.updateDevelopmentAmendmentAndSave(
            same(studySegment), eq(PropertyChange.create("cycleLength",command.getStudySegment().getCycleLength(),12)))).andReturn(study);
        replayMocks();
        command.apply();
        verifyMocks();
    }
}
