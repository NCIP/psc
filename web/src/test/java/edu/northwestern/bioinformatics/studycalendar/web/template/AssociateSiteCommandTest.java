package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;


/**
 * @author Nataliya Shurupova
 */
public class AssociateSiteCommandTest extends StudyCalendarTestCase {
    private AssociateSiteCommand command;
    private Study study;
    private Set<Object> sites;
    private Site site1, site2, site3, site4;
    private Set<Site> managingSites;
    private Boolean allSitesAccess;
    private StudyService studyService;

    protected void setUp() throws Exception {
        super.setUp();
        study = setId(-22, createNamedInstance("NU123", Study.class));
        site1 = setId(11, createNamedInstance("NU", Site.class));
        site2 = setId(12, createNamedInstance("CMH", Site.class));
        site3 = setId(13, createNamedInstance("RUSH", Site.class));
        site4 = setId(14, createNamedInstance("Managing", Site.class));

        sites = new LinkedHashSet<Object>();
        managingSites = new HashSet<Site>();
        sites.add(site1);
        sites.add(site2);
        sites.add(site3);
        allSitesAccess = false;
        studyService = registerMockFor(StudyService.class);
        command = new AssociateSiteCommand(study, studyService, sites, managingSites, allSitesAccess);
    }

    public void testValidateWithNoSiteSelected() {
        replayMocks();
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
    }

    public void testValidateWithAvailableSiteSelected() {
        managingSites.add(site2);

        command = new AssociateSiteCommand(study, studyService, sites, managingSites, allSitesAccess);
        replayMocks();
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);

        verifyMocks();
        assertEquals("Wrong error count", 0, errors.getErrorCount());
    }

    public void testRemoveSiteFromStudy() {
        managingSites.add(site1);
        managingSites.add(site2);
        managingSites.add(site4);

        sites.add(site1);
        study.addManagingSite(site1);
        study.addManagingSite(site2);
        study.addManagingSite(site3);
        study.addManagingSite(site4);
        Set<Site> before = study.getManagingSites();
        int beforeSize = before.size();
        command = new AssociateSiteCommand(study, studyService, sites, managingSites, allSitesAccess);
        studyService.save(study);
        replayMocks();
            command.buildUserSitesGrid();
            command.apply();
            Set<Site> after = study.getManagingSites();
        verifyMocks();

        assertEquals("None sites is removed", beforeSize-1, after.size());
        assertFalse("Site is not removed", after.contains(site3));
    }

    public void testAddSiteToStudy() {
        managingSites.add(site1);
        managingSites.add(site2);
        managingSites.add(site4);

        sites.add(site1);
        study.addManagingSite(site1);
        study.addManagingSite(site4);
        Set<Site> before = study.getManagingSites();
        int beforeSize = before.size();

        command = new AssociateSiteCommand(study, studyService, sites, managingSites, allSitesAccess);
        studyService.save(study);
        replayMocks();
            command.buildUserSitesGrid();
            command.apply();
            Set<Site> after = study.getManagingSites();
        verifyMocks();

        assertEquals("None sites is removed", beforeSize+1, after.size());
        assertFalse("Site was in the list before", !before.contains(site2));
        assertTrue("Site is not added", after.contains(site2));
    }
}