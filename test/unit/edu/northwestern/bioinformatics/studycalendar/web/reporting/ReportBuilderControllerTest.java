package edu.northwestern.bioinformatics.studycalendar.web.reporting;


import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;

/**
 * @author Jaron Sampson
 */
public class ReportBuilderControllerTest extends ControllerTestCase {
	private ReportBuilderController controller;
	private SiteDao siteDao;

	protected void setUp() throws Exception {
		super.setUp();

		siteDao = registerDaoMockFor(SiteDao.class);

		controller = new ReportBuilderController();
		controller.setSiteDao(siteDao);
	}

	public void testRefData() throws Exception {
        List<Site> sites = new ArrayList<Site>();
//        List<Study> studies = new ArrayList<Study>();
//        List<Participant> participants = new ArrayList<Participant>();
        expect(siteDao.getAll()).andReturn(sites);

		replayMocks();

        Map map = controller.referenceData(request);

        verifyMocks();
        assertSame("Site lists not the same.", sites, map.get("sites"));
//        assertEquals("Studies list not empty.", studies, map.get("studies"));
//        assertEquals("Participants list not empty.", participants, map.get("participants"));
	}
	
}
