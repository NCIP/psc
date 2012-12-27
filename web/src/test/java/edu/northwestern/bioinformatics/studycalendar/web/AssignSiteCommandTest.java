/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.expect;
import org.springframework.validation.BindException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * @author John Dzak
 */
public class AssignSiteCommandTest extends StudyCalendarTestCase {
    private AssignSiteCommand command;
    private Site site;
    private Study nu123;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        command = new AssignSiteCommand(studyDao);

        site = createNamedInstance("Northwestern University", Site.class);
        nu123 = setId(-22, createNamedInstance("NU123", Study.class));

        command.setStudyId(nu123.getId());
        expect(studyDao.getById(nu123.getId())).andReturn(nu123);
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

    public void testValidateWithUsedStudySite() {
        StudySite studySite = createStudySite(nu123, site);
        studySite.setStudySubjectAssignments(asList(new StudySubjectAssignment()));

        unassignSite();
        
        BindException errors = validateAndReturnErrors();

        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.cannot.remove.site.from.study.because.subjects.assigned", errors.getGlobalError().getCode());
    }

    ////// Helper Methods
    private BindException validateAndReturnErrors() {
        replayMocks();
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
