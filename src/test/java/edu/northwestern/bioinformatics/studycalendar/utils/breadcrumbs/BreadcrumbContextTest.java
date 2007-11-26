package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbContextTest extends StudyCalendarTestCase {
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
        BreadcrumbContext context = BreadcrumbContext.create(plannedActivity, templateService);
        verifyMocks();

        assertSame(plannedActivity, context.getPlannedActivity());
        assertSame(period, context.getPeriod());
        assertSame(studySegment, context.getStudySegment());
        assertSame(epoch, context.getEpoch());
        assertSame(plannedCalendar, context.getPlannedCalendar());
    }
}
