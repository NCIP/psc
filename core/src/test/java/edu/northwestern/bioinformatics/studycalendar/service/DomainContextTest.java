/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;

import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class DomainContextTest extends StudyCalendarTestCase {
    private TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        templateService = registerMockFor(TemplateService.class);
    }

    public void testSetPlannedElementsUsesTemplateServiceToResolveAncestors() throws Exception {
        PlannedCalendar plannedCalendar = new PlannedCalendar();
        Epoch epoch = new Epoch();
        StudySegment studySegment = new StudySegment();
        Period period = new Period();
        PlannedActivity plannedActivity = new PlannedActivity();
        expect(templateService.findParent(plannedActivity)).andReturn(period);
        expect(templateService.findParent(period)).andReturn(studySegment);
        expect(templateService.findParent(studySegment)).andReturn(epoch);
        expect(templateService.findParent(epoch)).andReturn(plannedCalendar);

        replayMocks();
        DomainContext context = DomainContext.create(plannedActivity, templateService);
        verifyMocks();

        assertSame(plannedActivity, context.getPlannedActivity());
        assertSame(period, context.getPeriod());
        assertSame(studySegment, context.getStudySegment());
        assertSame(epoch, context.getEpoch());
        assertSame(plannedCalendar, context.getPlannedCalendar());
    }

    public void testCloneReplacesBeanWrapper() throws Exception {
        Subject a = Fixtures.createSubject("A", "lpha");
        Subject b = Fixtures.createSubject("B", "eta");

        DomainContext dc = DomainContext.create(a, templateService);

        DomainContext copy = dc.clone();
        copy.setSubject(b);

        assertEquals("Wrapper not replaced", "B", copy.getProperty("subject.firstName"));
    }

    public void testCloneDoesNotCloneDomainObjects() throws Exception {
        Study s = Fixtures.createBasicTemplate();
        DomainContext dc = DomainContext.create(s, templateService);
        DomainContext copy = dc.clone();

        assertSame("Domain object replaced", s, copy.getStudy());
    }
}
