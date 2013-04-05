/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.suite.authorization.SiteMapping;
import gov.nih.nci.cabig.ctms.suite.authorization.StudyMapping;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class OsgiSuiteRoleMembershipLoaderTest {
    private OsgiSuiteRoleMembershipLoader loader;

    private MockRegistry mocks;
    private SuiteRoleMembershipLoader delegate;
    private OsgiLayerTools osgiLayerTools;
    private SiteMapping<Site> siteMapping;
    private StudyMapping<Study> studyMapping;

    @Before
    public void before() throws Exception {
        mocks = new MockRegistry();
        osgiLayerTools = mocks.registerMockFor(OsgiLayerTools.class);
        siteMapping = mocks.registerMockFor(PscSiteMapping.class);
        studyMapping = mocks.registerMockFor(PscStudyMapping.class);

        delegate = mocks.registerMockFor(SuiteRoleMembershipLoader.class);
        expect(osgiLayerTools.getRequiredService(SuiteRoleMembershipLoader.class)).
            andStubReturn(delegate);

        loader = new OsgiSuiteRoleMembershipLoader();
        loader.setOsgiLayerTools(osgiLayerTools);
        loader.setSiteMapping(siteMapping);
        loader.setStudyMapping(studyMapping);
    }

    @Test
    public void itDelegatesGetRolesToOsgiLayer() throws Exception {
        expect(delegate.getRoleMemberships(4)).
            andReturn(Collections.<SuiteRole, SuiteRoleMembership>emptyMap());
        mocks.replayMocks();

        loader.getRoleMemberships(4);
        mocks.verifyMocks();
    }

    @Test
    public void itDelegatesGetProvisioningRolesToOsgiLayer() throws Exception {
        expect(delegate.getProvisioningRoleMemberships(8)).
            andReturn(Collections.<SuiteRole, SuiteRoleMembership>emptyMap());
        mocks.replayMocks();

        loader.getProvisioningRoleMemberships(8);
        mocks.verifyMocks();
    }

    @Test
    public void itRecreatesRoleMembershipsWithAllSites() throws Exception {
        expectSingleRoleMembership(osgiSrm(SuiteRole.DATA_READER).forAllSites());

        SuiteRoleMembership actual = getActualRoleMemberships().get(SuiteRole.DATA_READER);
        assertThat(actual.isAllSites(), is(true));
        mocks.verifyMocks();
    }

    @Test
    public void itRecreatesRoleMembershipsWithAllStudies() throws Exception {
        expectSingleRoleMembership(osgiSrm(SuiteRole.DATA_ANALYST).forAllStudies());

        SuiteRoleMembership actual = getActualRoleMemberships().get(SuiteRole.DATA_ANALYST);
        assertThat(actual.isAllStudies(), is(true));
        mocks.verifyMocks();
    }

    @Test
    public void itRecreatesRoleMembershipsWithParticularSites() throws Exception {
        expectSingleRoleMembership(osgiSrm(SuiteRole.DATA_ANALYST).forSites("sT", "sY"));

        SuiteRoleMembership actual = getActualRoleMemberships().get(SuiteRole.DATA_ANALYST);
        assertThat(actual.getSiteIdentifiers(), is(Arrays.asList("sT", "sY")));
        mocks.verifyMocks();
    }

    @Test
    public void itRecreatesRoleMembershipsWithParticularStudies() throws Exception {
        expectSingleRoleMembership(osgiSrm(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER).forStudies("B", "T"));

        SuiteRoleMembership actual = getActualRoleMemberships().get(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        assertThat(actual.getStudyIdentifiers(), is(Arrays.asList("B", "T")));
        mocks.verifyMocks();
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void itRecreatesRoleMembershipsSoThatTheyCanLoadSiteObjects() throws Exception {
        expectSingleRoleMembership(osgiSrm(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER).forSites("sH"));
        Site site = Fixtures.createNamedInstance("sH", Site.class);
        expect(siteMapping.getApplicationInstances(Arrays.asList("sH"))).
            andReturn(Arrays.asList(site));

        SuiteRoleMembership actual = getActualRoleMemberships().get(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        assertThat(actual, is(not(nullValue())));
        assertThat((List<Site>) actual.getSites(), is(Arrays.asList(site)));
        mocks.verifyMocks();
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void itRecreatesRoleMembershipsSoThatTheyCanLoadStudyObjects() throws Exception {
        expectSingleRoleMembership(osgiSrm(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forStudies("G"));
        Study study = Fixtures.createNamedInstance("G", Study.class);
        expect(studyMapping.getApplicationInstances(Arrays.asList("G"))).
            andReturn(Arrays.asList(study));

        SuiteRoleMembership actual = getActualRoleMemberships().get(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertThat(actual, is(not(nullValue())));
        assertThat((List<Study>) actual.getStudies(), is(Arrays.asList(study)));
        mocks.verifyMocks();
    }

    private void expectSingleRoleMembership(SuiteRoleMembership srm) {
        expect(delegate.getProvisioningRoleMemberships(1)).
            andReturn(Collections.singletonMap(srm.getRole(), srm));
    }

    private Map<SuiteRole, SuiteRoleMembership> getActualRoleMemberships() {
        mocks.replayMocks();
        return loader.getProvisioningRoleMemberships(1);
    }

    private SuiteRoleMembership osgiSrm(SuiteRole role) {
        return new SuiteRoleMembership(role, null, null);
    }
}
