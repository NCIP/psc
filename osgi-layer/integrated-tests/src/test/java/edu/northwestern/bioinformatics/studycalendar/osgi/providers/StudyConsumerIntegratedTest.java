/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.providers;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.mock.MockDataProviderTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions;

import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestHelper.*;

/**
 * @author Jalpa Patel
 */
public class StudyConsumerIntegratedTest extends OsgiLayerIntegratedTestCase {
    private static final String MOCK_PROVIDERS_SYMBOLIC_NAME = "edu.northwestern.bioinformatics.psc-providers-mock";
    private Study study;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        study = Fixtures.createSingleEpochStudy("StudyName", "E", "S1", "S2");
        Fixtures.addSecondaryIdentifier(study, "nct", "NCT00003641");
        study.setProvider(MockDataProviderTools.PROVIDER_TOKEN);
        study.setAssignedIdentifier("NCT00003641");
        getStudyService().save(study);
        startBundle(MOCK_PROVIDERS_SYMBOLIC_NAME, StudyProvider.class.getName());
    }

    @Override
    public void tearDown() throws Exception {
        stopBundle(MOCK_PROVIDERS_SYMBOLIC_NAME);

        getStudyService().getStudyDao().delete(study);
        super.tearDown();
    }

    public void testRefreshDoesNotFail() throws Exception {
        Study refreshed = getStudyConsumer().refresh(study);
        assertEquals("NCT00003641", refreshed.getAssignedIdentifier());
        assertNotNull("Could not find test study", refreshed);
        assertNotNull("Test study not refreshed", refreshed.getLastRefresh());
        MoreJUnitAssertions.assertDatesClose("Not refreshed recently", new Date(), refreshed.getLastRefresh(), 25000);
    }

    ////// HELPERS

    private StudyService getStudyService() {
        return ((StudyService) getApplicationContext().getBean("studyService"));
    }

    private StudyConsumer getStudyConsumer() {
        return (StudyConsumer) getApplicationContext().getBean("studyConsumerBean");
    }
}
