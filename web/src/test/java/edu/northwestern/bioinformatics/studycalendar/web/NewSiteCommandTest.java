/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import static org.easymock.EasyMock.expect;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;

/**
 * @author Jalpa Patel
 */
public class NewSiteCommandTest extends StudyCalendarTestCase {
    private Site site;
    private SiteService siteService;
    private NewSiteCommand command;

    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        site = new Site();
        command = new NewSiteCommand(site, siteService);
    }

    public void testCreateSite() throws Exception {
        site.setName("Northwesten Uni");
        site.setAssignedIdentifier("Nu");
        expect(siteService.createOrUpdateSite(site)).andReturn(site);
        replayMocks();
        Site created = command.createSite();
        verifyMocks();

        assertEquals("Site name doesn't match", site.getName(), created.getName());
        assertEquals("Site assignedIdentifier doesn't match", site.getAssignedIdentifier(), created.getAssignedIdentifier());
    }

    public void testCreateSiteIfAssignedIdentifierIsNull() throws Exception {
        site.setName("Northwesten Uni");
        expect(siteService.createOrUpdateSite(site)).andReturn(site);
        replayMocks();
        Site created = command.createSite();
        verifyMocks();

        assertNotNull("AssignedIdentifier should not be null", created.getAssignedIdentifier());
        assertEquals("Site assignedIdentifier doesn't match", site.getName(), created.getAssignedIdentifier());
    }

    public void testValidateWhenSiteNameEmpty() throws Exception {
        replayMocks();
        BindException errors =  new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.site.name.is.empty", errors.getFieldError().getCode());
    }

    public void testValidateWhenSiteNameExists() throws Exception {
        site.setName("Northwestern Uni");
        expect(siteService.getByName(site.getName())).andReturn(site);
        replayMocks();
        BindException errors =  new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertEquals("Wrong error code", "error.site.name.already.exists", errors.getFieldError().getCode());
    }

    public void testValidateWhenSiteAssignedIdentifierExists() throws Exception {
        site.setName("Northwestern Uni12");
        site.setAssignedIdentifier("Nu");
        expect(siteService.getByName(site.getName())).andReturn(null);
        expect(siteService.getByAssignedIdentifier(site.getAssignedIdentifier())).andReturn(site);
        replayMocks();
        BindException errors =  new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertEquals("Wrong error code", "error.site.assignedIdentifier.already.exists", errors.getFieldError().getCode());
    }
}
