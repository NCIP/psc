package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import static edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ReleasedTemplateTest extends StudyCalendarTestCase {
    private ReleasedTemplate template;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = createBasicTemplate("Etc");
        template = new ReleasedTemplate(study, true);
    }

    public void testDisplayNameIncludesAmendmentIfAmended() throws Exception {
        Amendment amendment = createAmendment("No", DateTools.createDate(2007, Calendar.JUNE, 3));
        amendment.setPreviousAmendment(study.getAmendment());
        study.setAmendment(amendment);
        assertEquals("Etc [06/03/2007 (No)]", template.getDisplayName());
    }

    public void testDisplayNameDoesNotIncludesAmendmentIfInitial() throws Exception {
        assertEquals("Etc", template.getDisplayName());
    }
}
