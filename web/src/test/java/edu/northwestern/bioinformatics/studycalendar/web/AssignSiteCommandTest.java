package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;

import static java.util.Collections.singletonList;

/**
 * @author John Dzak
 */
public class AssignSiteCommandTest extends StudyCalendarTestCase {
    private AssignSiteCommand command;
    private Site site;

    protected void setUp() throws Exception {
        super.setUp();

        command = new AssignSiteCommand();

        site = createNamedInstance("Northwestern University", Site.class);
    }

    public void testValidateWithAvailableSiteSelected() {
        assigningSite();

        BindException errors = validateAndReturnErrors();

        assertEquals("Wrong error count", 0, errors.getErrorCount());
    }

    public void testValidateWithoutAvailableSiteSelected() {
        command.setAssign(true);

        BindException errors = validateAndReturnErrors();

        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.please.select.an.available.site", errors.getGlobalError().getCode());
    }

    public void testValidateWithAssignedSiteSelected() {
        unassignSite();

        BindException errors = validateAndReturnErrors();

        assertEquals("Wrong error count", 0, errors.getErrorCount());
    }
    
    public void testValidateWithoutAssignedSiteSelected() {
        command.setAssign(false);

        BindException errors = validateAndReturnErrors();

        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.please.select.an.assigned.site", errors.getGlobalError().getCode());
    }

    ////// Helper Methods
    private BindException validateAndReturnErrors() {
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        return errors;
    }

    private void assigningSite() {
        command.setAssign(true);
        command.setAvailableSites(singletonList(site));
    }

    private void unassignSite() {
        command.setAssign(false);
        command.setAssignedSites(singletonList(site));
    }
}
