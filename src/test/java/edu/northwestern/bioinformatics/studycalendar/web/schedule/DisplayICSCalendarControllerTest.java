package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

/**
 * @author Saurabh Agrawal
 */
@SuppressWarnings("unchecked")
public class DisplayICSCalendarControllerTest extends junit.framework.TestCase {

	protected MockHttpServletRequest request;

	protected MockHttpServletResponse response;

	protected MockServletContext servletContext;

	protected MockHttpSession session;

	private DisplayICSCalendarController controller;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		controller = new DisplayICSCalendarController();
		controller.setStudyParticipantAssignmentDao(new MockStudyParticipantDao());

	}

	public void testHandle() throws Exception {
		request.setMethod("GET");
		String grid = "adefg-higj";
		request.setPathInfo("/cal/schedule/display/" + grid + ".ics");

		controller.handleRequest(request, response);

	}

	public void testSupportedMethod() throws Exception {
		request.setMethod("POST");
		try {
			controller.handleRequest(request, response);
			fail("post not supported");
		}
		catch (org.springframework.web.HttpRequestMethodNotSupportedException e) {

		}
		String grid = "adefg-higj";
		request.setPathInfo("/cal/schedule/display/" + grid + ".ics");
		request.setMethod("GET");
		controller.handleRequest(request, response);

	}

	private class MockStudyParticipantDao extends StudyParticipantAssignmentDao {
		@Override
		public StudyParticipantAssignment getByGridId(final String gridId) {

			return new StudyParticipantAssignment();
		}
	}
}