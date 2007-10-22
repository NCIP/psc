package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

/**
 * @author Saurabh Agrawal
 */
@SuppressWarnings("unchecked")
public class DisplayICSCalendarControllerTest extends junit.framework.TestCase {

	protected MockHttpServletRequest request;

	protected MockHttpServletResponse response;

	private DisplayICSCalendarController controller;

	private String grid = "adefg-higj";

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

		request.setPathInfo("/cal/schedule/display/" + grid + ".ics");
		controller.handleRequest(request, response);

		assertEquals("response should return content type of calendar", "text/calendar", response.getContentType());
		assertEquals("response should return file of type ics", "attachment; filename=lastName-firstName-.ics",
				response.getHeader("Content-Disposition"));

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
			StudyParticipantAssignment studyParticipantAssignment = new StudyParticipantAssignment();
			studyParticipantAssignment.setGridId(gridId);
			Participant participant = Fixtures.createParticipant("firstName", "lastName");
			studyParticipantAssignment.setParticipant(participant);

			return studyParticipantAssignment;
		}
	}
}