package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class DevelopmentTemplateTest extends StudyCalendarTestCase {
    private DevelopmentTemplate template;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = createInDevelopmentBasicTemplate("Etc");
        template = new DevelopmentTemplate(study);
    }

    public void testDisplayNameIncludesDevelopmentAmendmentNameWhenPreviouslyReleased() throws Exception {
        study.getDevelopmentAmendment().setName(null);
        study.getDevelopmentAmendment().setDate(DateTools.createDate(2006, Calendar.JULY, 9));
        study.setAmendment(createAmendments(DateTools.createDate(2006, Calendar.MARCH, 3)));

        assertEquals("Etc [07/09/2006]", template.getDisplayName());
    }

    public void testDisplayNameDoesNotIncludeDevAmendmentNameWhenInitial() throws Exception {
        assertEquals("Etc", template.getDisplayName());
    }
}
